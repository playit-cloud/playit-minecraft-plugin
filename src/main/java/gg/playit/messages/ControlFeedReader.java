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

            if (responseType == 7) {
                /* AgentPortMapping - not used by Minecraft plugin */
                return readAndDiscardAgentPortMapping(in);
            }

            if (responseType == 8) {
                return readUdpChannelDetails(in);
            }

            throw new DecodeException("response type not implemented yet: " + responseType);
        }

        /* NewClientOld (type 2): connect_addr, peer_addr, claim_instructions, tunnel_server_id, data_center_id */
        if (feedType == 2) {
            var res = new NewClient();
            res.connectAddr = new SocketAddr();
            res.connectAddr.readFrom(in);

            res.peerAddr = new SocketAddr();
            res.peerAddr.readFrom(in);

            var claim = readClaimInstructions(in);
            res.claimAddress = claim.address;
            res.claimToken = claim.token;

            res.tunnelServerId = in.getLong();
            res.dataCenterId = in.getInt();
            res.tunnelId = 0;
            res.portOffset = 0;

            return res;
        }

        /* NewClient (type 3): connect_addr, peer_addr, data_center_id, tunnel_id, port_offset, claim_instructions */
        if (feedType == 3) {
            var res = new NewClient();
            res.connectAddr = new SocketAddr();
            res.connectAddr.readFrom(in);

            res.peerAddr = new SocketAddr();
            res.peerAddr.readFrom(in);

            res.dataCenterId = in.getInt();
            res.tunnelId = in.getLong();
            res.portOffset = Short.toUnsignedInt(in.getShort());

            var claim = readClaimInstructions(in);
            res.claimAddress = claim.address;
            res.claimToken = claim.token;

            res.tunnelServerId = 0;

            return res;
        }

        throw new DecodeException("feed type not implemented yet: " + feedType);
    }

    private static ClaimInstructions readClaimInstructions(ByteBuffer in) {
        var address = new SocketAddr();
        address.readFrom(in);

        var tokenLength = in.getLong();
        if (tokenLength < 0 || tokenLength > 65536) {
            throw new DecodeException("invalid claim token length: " + tokenLength);
        }
        var token = new byte[(int) tokenLength];
        in.get(token);

        return new ClaimInstructions(address, token);
    }

    private static ControlFeed readAndDiscardAgentPortMapping(ByteBuffer in) {
        /* PortRange + Optional<AgentPortMappingFound> - skip for now */
        throw new DecodeException("AgentPortMapping not supported");
    }

    private static ControlFeed readUdpChannelDetails(ByteBuffer in) {
        var tunnelAddr = new SocketAddr();
        tunnelAddr.readFrom(in);
        var tokenLen = in.getLong();
        if (tokenLen < 0 || tokenLen > 65536) {
            throw new DecodeException("invalid udp channel token length: " + tokenLen);
        }
        var token = new byte[(int) tokenLen];
        in.get(token);
        return new UdpChannelDetails(tunnelAddr, token);
    }

    public interface ControlFeed {
    }

    public static class ClaimInstructions {
        public final SocketAddr address;
        public final byte[] token;

        public ClaimInstructions(SocketAddr address, byte[] token) {
            this.address = address;
            this.token = token;
        }
    }

    public static class NewClient implements ControlFeed {
        public SocketAddr connectAddr;
        public SocketAddr peerAddr;
        public SocketAddr claimAddress;
        public byte[] claimToken;
        public long tunnelServerId;
        public long tunnelId;
        public int dataCenterId;
        public int portOffset;

        @Override
        public String toString() {
            return "NewClient{" +
                    "connectAddr=" + connectAddr +
                    ", peerAddr=" + peerAddr +
                    ", claimAddress=" + claimAddress +
                    ", claimToken=" + Arrays.toString(claimToken) +
                    ", tunnelServerId=" + tunnelServerId +
                    ", tunnelId=" + tunnelId +
                    ", dataCenterId=" + dataCenterId +
                    ", portOffset=" + portOffset +
                    '}';
        }
    }

    public static class UdpChannelDetails implements ControlFeed {
        public final SocketAddr tunnelAddr;
        public final byte[] token;

        public UdpChannelDetails(SocketAddr tunnelAddr, byte[] token) {
            this.tunnelAddr = tunnelAddr;
            this.token = token;
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
        /** 0 when not set, otherwise session expiry timestamp */
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

            /* session_expire_at: Option<u64> - 0=absent, 1=present+u64 */
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
