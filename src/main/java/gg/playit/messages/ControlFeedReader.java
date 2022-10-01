package gg.playit.messages;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ControlFeedReader {

    public static ControlFeed read(ByteBuffer in) {
        in.order(ByteOrder.BIG_ENDIAN);

        var feedType = in.getInt();
        /* Response */
        if (feedType == 1) {
            var requestId = in.getLong();

            var responseType = in.getInt();

            /* Pong */
            if (responseType == 1) {
                var pong = new Pong();
                pong.readFrom(requestId, in);
                return pong;
            }

            if (responseType == 2) {
                return Error.InvalidSignature;
            }

            if (responseType == 3) {
                return Error.Unauthorized;
            }

            if (responseType == 4) {
                return Error.RequestQueued;
            }

            if (responseType == 5) {
                return Error.TryAgainLater;
            }

            if (responseType == 6) {
                var res = new AgentRegistered();
                res.requestId = requestId;
                res.id = new AgentSessionId();
                res.id.readFrom(in);
                res.expiresAt = in.getLong();
                return res;
            }

            throw new DecodeException("response type not implemented yet: " + responseType);
        }

        if (feedType == 2) {
            var res = new NewClient();
            res.connectAddr = new SocketAddr();
            res.connectAddr.readFrom(in);

            res.peerAddr = new SocketAddr();
            res.peerAddr.readFrom(in);

            res.claimAddress = new SocketAddr();
            res.claimAddress.readFrom(in);

            var tokenLength = in.getLong();
            res.claimToken = new byte[(int) tokenLength];
            in.get(res.claimToken);

            res.tunnelServerId = in.getLong();
            res.dataCenterId = in.getInt();

            return res;
        }

        throw new DecodeException("feed type not implemented yet: " + feedType);
    }

    public interface ControlFeed {
    }

    public static class NewClient implements ControlFeed {
        public SocketAddr connectAddr;
        public SocketAddr peerAddr;
        public SocketAddr claimAddress;
        public byte[] claimToken;
        public long tunnelServerId;
        public int dataCenterId;

        @Override
        public String toString() {
            return "NewClient{" +
                    "connectAddr=" + connectAddr +
                    ", peerAddr=" + peerAddr +
                    ", claimAddress=" + claimAddress +
                    ", claimToken=" + Arrays.toString(claimToken) +
                    ", tunnelServerId=" + tunnelServerId +
                    ", dataCenterId=" + dataCenterId +
                    '}';
        }
    }

    public static class AgentRegistered implements ControlFeed {
        public long requestId;

        public AgentSessionId id;
        public long expiresAt;

        @Override
        public String toString() {
            return "AgentRegistered{" +
                    "requestId=" + requestId +
                    ", id=" + id +
                    ", expiresAt=" + expiresAt +
                    '}';
        }
    }

    public enum Error implements ControlFeed {
        InvalidSignature,
        Unauthorized,
        RequestQueued,
        TryAgainLater
    }

    public static class Pong implements ControlFeed {
        public long requestId;
        public long requestNow;
        public long serverNow;
        public long serverId;
        public int dataCenterId;
        public SocketAddr clientAddr;
        public SocketAddr tunnelAddr;
        public long sessionExpireAt;

        private void readFrom(long requestId, ByteBuffer in) {
            in.order(ByteOrder.BIG_ENDIAN);

            this.requestId = requestId;

            requestNow = in.getLong();
            serverNow = in.getLong();
            serverId = in.getLong();
            dataCenterId = in.getInt();

            clientAddr = new SocketAddr();
            clientAddr.readFrom(in);

            tunnelAddr = new SocketAddr();
            tunnelAddr.readFrom(in);

            byte hasSessionExpireAt = in.get();
            if (hasSessionExpireAt == 0) {
                sessionExpireAt = 0;
            } else if (hasSessionExpireAt == 1) {
                sessionExpireAt = in.getLong();
            } else {
                throw new DecodeException("expected 0/1 for Optional session_expire_at but got: " + hasSessionExpireAt);
            }
        }

        @Override
        public String toString() {
            return "Pong{" +
                    "requestId=" + requestId +
                    ", requestNow=" + requestNow +
                    ", serverNow=" + serverNow +
                    ", serverId=" + serverId +
                    ", dataCenterId=" + dataCenterId +
                    ", clientAddr=" + clientAddr +
                    ", tunnelAddr=" + tunnelAddr +
                    ", sessionExpireAt=" + sessionExpireAt +
                    '}';
        }
    }
}
