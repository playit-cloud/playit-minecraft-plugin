package gg.playit.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ControlRequestWriter {
    public static RequestBodyWriter requestId(ByteBuffer out, long id) {
        out.order(ByteOrder.BIG_ENDIAN);
        out.putLong(id);

        var req = new RequestBodyWriter();
        req.out = out;
        return req;
    }

    public static class RequestBodyWriter {
        private ByteBuffer out;

        public void ping(long now, AgentSessionId sessionId) {
            /* write ping id */
            out.putInt(1);

            out.putLong(now);
            if (sessionId == null) {
                out.put((byte) 0);
            } else {
                out.put((byte) 1);
                sessionId.writeTo(out);
            }
            this.out = null;
        }

        public void keepAlive(AgentSessionId sessionId) {
            /* write keep alive id */
            out.putInt(3);
            sessionId.writeTo(out);
            this.out = null;
        }

        public void registerBytes(byte[] signedRegisterBytes) {
            out.put(signedRegisterBytes);
            this.out = null;
        }
    }
}
