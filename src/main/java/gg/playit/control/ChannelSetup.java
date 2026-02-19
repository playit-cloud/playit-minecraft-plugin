package gg.playit.control;

import gg.playit.api.ApiClient;
import gg.playit.api.ApiClientException;
import gg.playit.api.model.ApiResult;
import gg.playit.api.model.ApiSuccess;
import gg.playit.api.model.enums.Platform;
import gg.playit.api.model.request.AgentVersion;
import gg.playit.api.model.request.ReqAgentsRoutingGet;
import gg.playit.api.model.request.ReqProtoRegister;
import gg.playit.api.model.response.AgentRouting;
import gg.playit.api.model.response.SignedAgentKey;
import gg.playit.messages.ControlFeedReader;
import gg.playit.messages.ControlRequestWriter;
import gg.playit.messages.DecodeException;
import gg.playit.minecraft.utils.DecoderException;
import gg.playit.minecraft.utils.Hex;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ChannelSetup {
    public static final int CONTROL_PORT = 5525;

    static Logger log = Logger.getLogger(ChannelSetup.class.getName());

    /**
     * Start channel setup using protocol v2. Requires secret key to fetch control addresses via API.
     *
     * @param secretKey   the agent secret key
     * @param agentVersion the agent version for proto register, or null to use {@link #DEFAULT_AGENT_VERSION}
     */
    public static FindSuitableChannel start(String secretKey, AgentVersion agentVersion) throws IOException {
        var apiClient = new ApiClient(secretKey);

        ApiResult<AgentRouting, ?> routingResult;
        try {
            routingResult = apiClient.agentsRoutingGet(new ReqAgentsRoutingGet(null));
        } catch (ApiClientException e) {
            throw new IOException("failed to get control addresses from API", e);
        }

        if (!(routingResult instanceof ApiSuccess<AgentRouting, ?> success)) {
            throw new IOException("agents routing get failed: " + routingResult);
        }

        var routing = success.data();
        var addresses = new ArrayList<InetSocketAddress>();

        /* Prefer IPv6 like Rust */
        if (routing.targets6() != null && !routing.disable_ip6()) {
            for (var ip : routing.targets6()) {
                try {
                    addresses.add(new InetSocketAddress(InetAddress.getByName(ip), CONTROL_PORT));
                } catch (UnknownHostException e) {
                    log.warning("failed to parse IPv6 target: " + ip);
                }
            }
        }
        if (routing.targets4() != null) {
            for (var ip : routing.targets4()) {
                try {
                    addresses.add(new InetSocketAddress(InetAddress.getByName(ip), CONTROL_PORT));
                } catch (UnknownHostException e) {
                    log.warning("failed to parse IPv4 target: " + ip);
                }
            }
        }

        if (addresses.isEmpty()) {
            throw new IOException("no control addresses returned from API");
        }

        var setup = new FindSuitableChannel();
        setup.options = addresses.toArray(new InetSocketAddress[0]);
        setup.apiClient = apiClient;
        setup.agentVersion = agentVersion;
        return setup;
    }

    public static class FindSuitableChannel {
        private InetSocketAddress[] options;
        private ApiClient apiClient;
        private AgentVersion agentVersion;

        public SetupRequireAuthentication findChannel() throws IOException {
            var socket = new DatagramSocket();

            /* 3 second timeout */
            socket.setSoTimeout(3000);

            var buffer = ByteBuffer.allocate(1024);
            {
                var builder = ControlRequestWriter.requestId(buffer, 1);
                builder.ping(System.currentTimeMillis(), null);
            }
            var bytesWritten = buffer.position();

            for (var option : options) {
                for (var i = 0; i < 3; ++i) {
                    try {
                        var packet = new DatagramPacket(buffer.array(), 0, bytesWritten, option);
                        socket.send(packet);

                        DatagramPacket rxPacket = new DatagramPacket(new byte[1024], 0, 1024);
                        socket.receive(rxPacket);

                        if (!rxPacket.getAddress().equals(option.getAddress()) || rxPacket.getPort() != option.getPort()) {
                            log.warning("got response from unexpected source: " + rxPacket.getAddress() + ", port: " + rxPacket.getPort());
                            continue;
                        }

                        var in = ByteBuffer.wrap(rxPacket.getData(), rxPacket.getOffset(), rxPacket.getLength());

                        try {
                            var message = ControlFeedReader.read(in);
                            if (message instanceof ControlFeedReader.Pong) {
                                var next = new SetupRequireAuthentication();
                                next.pong = (ControlFeedReader.Pong) message;
                                next.socket = socket;
                                next.address = option.getAddress();
                                next.port = option.getPort();
                                next.apiClient = apiClient;
                                next.agentVersion = agentVersion;
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
        private int port;
        private ApiClient apiClient;
        private AgentVersion agentVersion;

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

            byte[] registerBytes;
            try {
                var req = new ReqProtoRegister(
                        null,
                        2L,
                        agentVersion,
                        Platform.MinecraftPlugin,
                        pong.clientAddr.toString(),
                        pong.tunnelAddr.toString()
                );
                var result = apiClient.protoRegister(req);

                if (!(result instanceof ApiSuccess<SignedAgentKey, ?> success)) {
                    throw new IOException("proto register failed: " + result);
                }

                registerBytes = Hex.decodeHex(success.data().key());
            } catch (DecoderException e) {
                throw new IOException("failed to decode hex response from server", e);
            } catch (ApiClientException e) {
                throw new IOException("proto register API error", e);
            }

            var registerRequest = ByteBuffer.allocate(1024 + registerBytes.length);
            ControlRequestWriter.requestId(registerRequest, 100).registerBytes(registerBytes);

            var packet = new DatagramPacket(registerRequest.array(), registerRequest.arrayOffset(), registerRequest.position());
            packet.setAddress(this.address);
            packet.setPort(this.port);

            for (int i = 0; i < 4; i++) {
                this.socket.send(packet);
                var rxBuffer = new byte[1024];

                try {
                    DatagramPacket rxPacket = new DatagramPacket(rxBuffer, 1024);
                    this.socket.receive(rxPacket);

                    var packetData = ByteBuffer.wrap(rxPacket.getData(), rxPacket.getOffset(), rxPacket.getLength());
                    try {
                        var response = ControlFeedReader.read(packetData);

                        if (response instanceof ControlFeedReader.AgentRegistered registered) {
                            var channel = new PlayitControlChannel();
                            channel.apiClient = apiClient;
                            channel.socket = this.socket;
                            channel.controlAddress = this.address;
                            channel.controlPort = this.port;
                            channel.registered = registered;
                            channel.ogPong = this.pong;
                            channel.latestPong = this.pong;

                            this.socket = null;
                            return channel;
                        }

                        if (response instanceof ControlFeedReader.Error error) {
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
