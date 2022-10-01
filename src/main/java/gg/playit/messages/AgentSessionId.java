package gg.playit.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AgentSessionId {
    public long sessionId;
    public long accountId;
    public long agentId;

    public void writeTo(ByteBuffer out) {
        out.order(ByteOrder.BIG_ENDIAN);
        out.putLong(sessionId);
        out.putLong(accountId);
        out.putLong(agentId);
    }

    public void readFrom(ByteBuffer in) {
        in.order(ByteOrder.BIG_ENDIAN);
        this.sessionId = in.getLong();
        this.accountId = in.getLong();
        this.agentId = in.getLong();
    }
}
