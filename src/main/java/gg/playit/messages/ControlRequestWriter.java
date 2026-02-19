package gg.playit.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ControlRequestWriter {
    /** PingV2 request id */
    private static final int PING_V2_ID = 6;
    /** AgentKeepAliveV1 request id */
    private static final int KEEP_ALIVE_ID = 3;

    public static RequestBodyWriter requestId(ByteBuffer out, long id) {
        out.order(ByteOrder.BIG_ENDIAN);
        out.putLong(id);

        var req = new RequestBodyWriter();
        req.out = out;
        return req;
    }

    public static class RequestBodyWriter {
        private ByteBuffer out;

        /**
         * Write PingV2 (id 6): now (u64), current_ping (optional u32), session_id (optional AgentSessionId).
         */
        public void ping(long now, AgentSessionId sessionId) {
            out.putInt(PING_V2_ID);

            out.putLong(now);
            /* current_ping: Option<u32> - 0=absent, 1=present+u32. We skip for simplicity. */
            out.put((byte) 0);
            /* session_id: Option<AgentSessionId> - 0=absent, 1=present */
            if (sessionId == null) {
                out.put((byte) 0);
            } else {
                out.put((byte) 1);
                sessionId.writeTo(out);
            }
            this.out = null;
        }

        public void keepAlive(AgentSessionId sessionId) {
            out.putInt(KEEP_ALIVE_ID);
            sessionId.writeTo(out);
            this.out = null;
        }

        /**
         * Write raw register bytes (RawSlice). Format: request_id + raw bytes.
         * No ControlRequest wrapper - same as Rust RawSlice.
         */
        public void registerBytes(byte[] signedRegisterBytes) {
            out.put(signedRegisterBytes);
            this.out = null;
        }
    }
}
