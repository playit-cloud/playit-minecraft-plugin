package gg.playit.minecraft;

import gg.playit.minecraft.utils.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.bukkit.Server;

import java.net.InetSocketAddress;

public class PlayitTcpTunnel {
    static Logger log = new Logger(PlayitTcpTunnel.class.getName());

    private final InetSocketAddress trueIp;
    private final EventLoopGroup group;
    private final String connectionKey;
    private final PlayitConnectionTracker tracker;
    private final InetSocketAddress minecraftServerAddress;
    private final InetSocketAddress tunnelClaimAddress;
    private final byte[] tunnelClaimToken;
    private final Server server;

    private final int connectionTimeoutSeconds;

    public PlayitTcpTunnel(
            InetSocketAddress trueIp,
            EventLoopGroup group,
            PlayitConnectionTracker tracker,
            String connectionKey,
            InetSocketAddress minecraftServerAddress,
            InetSocketAddress tunnelClaimAddress,
            byte[] tunnelClaimToken,
            Server server,
            int connectionTimeoutSeconds
    ) {
        this.trueIp = trueIp;
        this.group = group;
        this.tracker = tracker;
        this.connectionKey = connectionKey;
        this.minecraftServerAddress = minecraftServerAddress;
        this.tunnelClaimAddress = tunnelClaimAddress;
        this.tunnelClaimToken = tunnelClaimToken;
        this.server = server;
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }

    private Channel minecraftChannel;
    private Channel tunnelChannel;

    public void start() {
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(group);
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.remoteAddress(this.tunnelClaimAddress);

        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) {
                tunnelChannel = socketChannel;
                socketChannel.pipeline().addLast(new TunnelConnectionHandler());
            }
        });

        log.debug("start connection to " + tunnelClaimAddress + " to claim client");
        clientBootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.warning("failed to establish connection to tunnel claim" + tunnelClaimAddress);
                disconnected();
                return;
            }

            log.debug("connected to tunnel server, sending claim token");

            future.channel().writeAndFlush(Unpooled.wrappedBuffer(tunnelClaimToken)).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warning("failed to send claim token");
                } else {
                    log.debug("claim token sent");
                }
            });
        });
    }

    private void disconnected() {
        this.tracker.removeConnection(connectionKey);
    }

    @ChannelHandler.Sharable
    private class TunnelConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {
        TunnelConnectionHandler() {
            super(false);
        }

        private int confirmBytesRemaining = 8;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
            if (confirmBytesRemaining > 0) {
                if (byteBuf.readableBytes() < confirmBytesRemaining) {
                    confirmBytesRemaining -= byteBuf.readableBytes();
                    byteBuf.readBytes(byteBuf.readableBytes());
                    byteBuf.release();
                    ctx.read();
                    return;
                }

                byteBuf.readBytes(confirmBytesRemaining);
                confirmBytesRemaining = 0;

                log.debug("connection to tunnel server has been established");

                if (addChannelToMinecraftServer()) {
                    log.debug("added channel to minecraft server");
                    return;
                }

                var minecraftClient = new Bootstrap();
                minecraftClient.group(group);
                minecraftClient.option(ChannelOption.TCP_NODELAY, true);
                minecraftClient.channel(NioSocketChannel.class);
                minecraftClient.remoteAddress(minecraftServerAddress);

                minecraftClient.handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) {
                        minecraftChannel = socketChannel;
                        socketChannel.pipeline().addLast(new MinecraftConnectionHandler());
                    }
                });

                log.debug("connecting to minecraft server at " + minecraftServerAddress);
                minecraftClient.connect().addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        log.warning("failed to connect to local minecraft server");
                        ctx.disconnect();
                        disconnected();
                        return;
                    }

                    log.debug("connected to local minecraft server");

                    if (byteBuf.readableBytes() == 0) {
                        byteBuf.release();
                        ctx.read();
                    } else {
                        future.channel().writeAndFlush(byteBuf).addListener(f -> {
                            if (!f.isSuccess()) {
                                log.warning("failed to send data to minecraft server");
                                future.channel().disconnect();
                                ctx.disconnect();
                                disconnected();
                                return;
                            }

                            ctx.read();
                        });
                    }
                });

                return;
            }

            /* proxy data */
            minecraftChannel.writeAndFlush(byteBuf).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warning("failed to send data to minecraft server");
                    minecraftChannel.disconnect();
                    tunnelChannel.disconnect();
                    disconnected();
                    return;
                }

                ctx.read();
            });
        }

        private boolean addChannelToMinecraftServer() {
            ReflectionHelper reflect = new ReflectionHelper();
            log.debug("Reflect: " + reflect);

            Object minecraftServer = reflect.getMinecraftServer(server);
            if (minecraftServer == null) {
                log.debug("failed to get Minecraft server from Bukkit.getServer()");
                return false;
            }

            Object serverConnection = reflect.serverConnectionFromMCServer(minecraftServer);
            if (serverConnection == null) {
                log.debug("failed to get ServerConnection from Minecraft Server");
                return false;
            }

            Object legacyPingHandler = reflect.newLegacyPingHandler(serverConnection);
            if (legacyPingHandler == null) {
                log.debug("legacyPingHandler is null");
                return false;
            }

            Object packetSplitter = reflect.newPacketSplitter();
            if (packetSplitter == null) {
                log.debug("packetSplitter is null");
                return false;
            }

            Object packetDecoder = reflect.newServerBoundPacketDecoder();
            if (packetDecoder == null) {
                log.debug("packetDecoder is null");
                return false;
            }

            Object packetPrepender = reflect.newPacketPrepender();
            if (packetPrepender == null) {
                log.debug("packetPrepender is null");
                return false;
            }

            Object packetEncoder = reflect.newClientBoundPacketEncoder();
            if (packetEncoder == null) {
                log.debug("packetEncoder is null");
                return false;
            }

            Integer rateLimitNullable = reflect.getRateLimitFromMCServer(minecraftServer);
            if (rateLimitNullable == null) {
                rateLimitNullable = 0;
            }

            int rateLimit = rateLimitNullable;

            Object networkManager;
            if (rateLimit > 0) {
                networkManager = reflect.newNetworkManagerServer(rateLimit);
            } else {
                networkManager = reflect.newServerNetworkManager();
            }

            if (networkManager == null) {
                log.debug("networkManager is null");
                return false;
            }

            Object handshakeListener = reflect.newHandshakeListener(minecraftServer, networkManager);
            if (handshakeListener == null) {
                log.debug("handshakeListener is null");
                return false;
            }

            if (!reflect.networkManagerSetListener(networkManager, handshakeListener)) {
                log.debug("failed to set handshake listener on network manager");
                return false;
            }

            if (!reflect.setRemoteAddress(tunnelChannel, trueIp)) {
                log.warning("failed to set remote address to " + trueIp);
            }

            var channel = tunnelChannel.pipeline().removeLast();
            tunnelChannel.pipeline()
                    .addLast("timeout", new ReadTimeoutHandler(connectionTimeoutSeconds))
                    .addLast("legacy_query", (ChannelHandler) legacyPingHandler)
                    .addLast("splitter", (ChannelHandler) packetSplitter)
                    .addLast("decoder", (ChannelHandler) packetDecoder)
                    .addLast("prepender", (ChannelHandler) packetPrepender)
                    .addLast("encoder", (ChannelHandler) packetEncoder)
                    .addLast("packet_handler", (ChannelHandler) networkManager);

            if (!reflect.addToServerConnections(serverConnection, networkManager)) {
                log.debug("failed to add to server connections");

                tunnelChannel.pipeline().remove("timeout");
                tunnelChannel.pipeline().remove("legacy_query");
                tunnelChannel.pipeline().remove("splitter");
                tunnelChannel.pipeline().remove("decoder");
                tunnelChannel.pipeline().remove("prepender");
                tunnelChannel.pipeline().remove("encoder");
                tunnelChannel.pipeline().remove("packet_handler");

                tunnelChannel.pipeline().addLast(channel);

                return false;
            }

            tunnelChannel.pipeline().fireChannelActive();
            return true;
        }
    }

    private class MinecraftConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {
        MinecraftConnectionHandler() {
            super(false);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            tunnelChannel.writeAndFlush(msg).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warning("failed to send data to tunnel");
                    minecraftChannel.disconnect();
                    tunnelChannel.disconnect();
                    return;
                }

                ctx.read();
            });
        }
    }
}
