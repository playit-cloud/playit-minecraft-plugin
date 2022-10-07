package gg.playit.control;

import gg.playit.api.ApiClient;
import gg.playit.api.actions.SignAgentRegister;
import gg.playit.messages.ControlFeedReader;
import gg.playit.messages.ControlRequestWriter;
import gg.playit.messages.DecodeException;
import gg.playit.minecraft.utils.DecoderException;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class ChannelSetup {
    public static final int CONTROL_PORT = 5525;

    static Logger log = Logger.getLogger(ChannelSetup.class.getName());

    public static FindSuitableChannel start() throws UnknownHostException {
        InetAddress[] allByName = InetAddress.getAllByName("control.playit.gg");
        /* prefer IPv6 */
        Arrays.sort(allByName, Comparator.comparingLong(a -> -a.getAddress().length));

        FindSuitableChannel setup = new FindSuitableChannel();
        setup.options = allByName;
        return setup;
    }

    public static class FindSuitableChannel {
        private InetAddress[] options;

        public SetupRequireAuthentication findChannel() throws IOException {
            DatagramSocket socket = new DatagramSocket();

            /* 3 second timeout */
            socket.setSoTimeout(3000);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            {
                ControlRequestWriter.RequestBodyWriter builder = ControlRequestWriter.requestId(buffer, 1);
                builder.ping(0, null);
            }
            int bytesWritten = buffer.position();

            for (InetAddress option : options) {
                for (int i = 0; i < 3; ++i) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer.array(), 0, bytesWritten, new InetSocketAddress(option, CONTROL_PORT));
                        socket.send(packet);

                        DatagramPacket rxPacket = new DatagramPacket(new byte[1024], 0, 1024);
                        socket.receive(rxPacket);

                        if (!Arrays.equals(rxPacket.getAddress().getAddress(), option.getAddress()) || rxPacket.getPort() != CONTROL_PORT) {
                            log.warning("got response from unexpected source: " + rxPacket.getAddress() + ", port: " + rxPacket.getPort());
                            continue;
                        }

                        ByteBuffer in = ByteBuffer.wrap(rxPacket.getData(), rxPacket.getOffset(), rxPacket.getLength());

                        try {
                            ControlFeedReader.ControlFeed message = ControlFeedReader.read(in);
                            if (message instanceof ControlFeedReader.Pong) {
                                SetupRequireAuthentication next = new SetupRequireAuthentication();
                                next.pong = (ControlFeedReader.Pong) message;
                                next.socket = socket;
                                next.address = option;
                                return next;
                            } else {
                                log.warning("expected pong response but got: " + message);
                            }
                        } catch (DecodeException e) {
                            log.warning("Failed to decode pong response: " + e.message);
                        }

                    } catch (SocketTimeoutException ignore) {
                        log.warning("timeout waiting for pong response");
                    } catch (IOException error) {
                        log.warning("Got IO error working with :" + option + ", error : " + error);
                        break;
                    }
                }
            }

            socket.close();
            throw new IOException("failed to establish connection to tunnel");
        }
    }

    public static class SetupRequireAuthentication {
        private ControlFeedReader.Pong pong;
        private DatagramSocket socket;
        private InetAddress address;

        @Override
        public String toString() {
            return "SetupRequireAuthentication{" +
                    "pong=" + pong +
                    ", socket=" + socket +
                    ", address=" + address +
                    '}';
        }

        public PlayitControlChannel authenticate(String secretKey) throws IOException {
            if (this.socket == null) {
                throw new IOException("already used");
            }

            ByteBuffer registerRequest = ByteBuffer.allocate(1024);

            try {
                ApiClient client = new ApiClient(secretKey);
                SignAgentRegister req = new SignAgentRegister();
                req.agentVersion = 10_001;
                req.clientAddr = this.pong.clientAddr;
                req.tunnelAddr = this.pong.tunnelAddr;
                byte[] data = client.getSignedAgentRegisterData(req);
                ControlRequestWriter.requestId(registerRequest, 100).registerBytes(data);
            } catch (DecoderException e) {
                throw new IOException("failed parse hex response from server", e);
            }

            DatagramPacket packet = new DatagramPacket(registerRequest.array(), registerRequest.arrayOffset(), registerRequest.position());
            packet.setAddress(this.address);
            packet.setPort(CONTROL_PORT);

            for (int i = 0; i < 4; i++) {
                this.socket.send(packet);
                byte[] rxBuffer = new byte[1024];

                try {
                    DatagramPacket rxPacket = new DatagramPacket(rxBuffer, 1024);
                    this.socket.receive(rxPacket);

                    ByteBuffer packetData = ByteBuffer.wrap(rxPacket.getData(), rxPacket.getOffset(), rxPacket.getLength());
                    try {
                        ControlFeedReader.ControlFeed response = ControlFeedReader.read(packetData);

                        if (response instanceof ControlFeedReader.AgentRegistered) {
                            ControlFeedReader.AgentRegistered registered = (ControlFeedReader.AgentRegistered) response;
                            PlayitControlChannel channel = new PlayitControlChannel();
                            channel.apiClient = new ApiClient(secretKey);
                            channel.socket = this.socket;
                            channel.controlAddress = this.address;
                            channel.registered = registered;
                            channel.ogPong = this.pong;
                            channel.latestPong = this.pong;

                            this.socket = null;
                            return channel;
                        }

                        if (response instanceof ControlFeedReader.Error) {
                            ControlFeedReader.Error error = (ControlFeedReader.Error) response;
                            if (error == ControlFeedReader.Error.RequestQueued) {
                                log.info("request queued, waiting 1 second before resend");

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ignore) {
                                }

                                continue;
                            }

                            log.warning("got error from control feed: " + error);
                        }

                        break;
                    } catch (DecodeException | BufferUnderflowException error) {
                        log.warning("failed to decode register response: " + error);
                    }
                } catch (SocketTimeoutException ignore) {
                    log.warning("timeout waiting for register response");
                }
            }

            throw new IOException("failed to setup channel");
        }
    }
}
