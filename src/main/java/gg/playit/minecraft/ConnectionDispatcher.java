package gg.playit.minecraft;

import gg.playit.messages.ControlFeedReader;
import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;

public interface ConnectionDispatcher {
    void handleConnection(ConnectionDetails details);

    class ConnectionDetails {
        public ControlFeedReader.NewClient client;
        public EventLoopGroup nettyEventGroup;
        public String clientKey;
        public InetSocketAddress remoteAddress;
    }
}
