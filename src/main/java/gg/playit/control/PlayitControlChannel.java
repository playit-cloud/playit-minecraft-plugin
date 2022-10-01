package gg.playit.control;

import gg.playit.api.ApiClient;
import gg.playit.messages.ControlFeedReader;
import gg.playit.messages.ControlRequestWriter;
import gg.playit.messages.DecodeException;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import static gg.playit.control.ChannelSetup.CONTROL_PORT;

public class PlayitControlChannel implements Closeable {
    static Logger log = Logger.getLogger(ChannelSetup.class.getName());

    ApiClient apiClient;
    DatagramSocket socket;
    InetAddress controlAddress;
    ControlFeedReader.Pong ogPong;
    ControlFeedReader.Pong latestPong;
    ControlFeedReader.AgentRegistered registered;

    private final ByteBuffer sendBuffer = ByteBuffer.allocate(2048);

    private long lastKeepAlive;
    private long lastPing;

    public static PlayitControlChannel setup(String secretKey) throws IOException {
        try {
            return ChannelSetup
                    .start()
                    .findChannel()
                    .authenticate(secretKey);
        } catch (DecodeException | BufferUnderflowException error) {
            throw new IOException("failed to encoding / decoding data", error);
        }
    }

    public Optional<ControlFeedReader.ControlFeed> update() throws IOException {
        try {
            var now = Instant.now().toEpochMilli();

            if (now - lastPing > 5_000) {
                lastPing = now;
                this.sendPing(now);
            }

            var tillExpire = this.registered.expiresAt - now;
            if (tillExpire < 60_000 && 10_000 < now - lastKeepAlive) {
                log.info("send keep alive");
                lastKeepAlive = now;

                this.sendKeepAlive();
            }

            this.socket.setSoTimeout(3_000);

            DatagramPacket rxPacket = new DatagramPacket(new byte[2048], 0, 2048);

            try {
                this.socket.receive(rxPacket);
            } catch (SocketTimeoutException ignore) {
                return Optional.empty();
            }

            if (!Arrays.equals(rxPacket.getAddress().getAddress(), this.controlAddress.getAddress()) || rxPacket.getPort() != CONTROL_PORT) {
                log.warning("got packet from unexpected source: " + rxPacket.getAddress() + ", port: " + rxPacket.getPort());
                return Optional.empty();
            }

            var buffer = ByteBuffer.wrap(
                    rxPacket.getData(),
                    rxPacket.getOffset(),
                    rxPacket.getLength()
            );

            var read = ControlFeedReader.read(buffer);

            if (read instanceof ControlFeedReader.Pong pong) {
                this.latestPong = pong;

                if (pong.sessionExpireAt != 0) {
                    this.registered.expiresAt = pong.sessionExpireAt;
                }
            } else if (read instanceof ControlFeedReader.AgentRegistered registered) {
                this.registered = registered;
            }

            return Optional.of(read);
        } catch (Exception e) {
            throw new IOException("got unexpected error", e);
        }
    }

    private void sendPing(long now) throws IOException {
        sendBuffer.clear();
        ControlRequestWriter.requestId(sendBuffer, 100).ping(now, this.registered.id);
        this.sendPacket();
    }

    private void sendKeepAlive() throws IOException {
        sendBuffer.clear();
        ControlRequestWriter.requestId(sendBuffer, 100).keepAlive(this.registered.id);
        this.sendPacket();
    }

    private void sendPacket() throws IOException {
        DatagramPacket p = new DatagramPacket(sendBuffer.array(), sendBuffer.arrayOffset(), sendBuffer.position());
        p.setAddress(this.controlAddress);
        p.setPort(CONTROL_PORT);
        this.socket.send(p);
    }

    @Override
    public String toString() {
        return "ControlChannel{" +
                "apiClient=" + apiClient +
                ", socket=" + socket +
                ", controlAddress=" + controlAddress +
                ", ogPong=" + ogPong +
                ", latestPong=" + latestPong +
                ", registered=" + registered +
                '}';
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
