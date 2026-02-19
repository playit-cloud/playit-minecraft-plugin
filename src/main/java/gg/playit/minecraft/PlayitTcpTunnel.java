package gg.playit.minecraft;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bukkit.Server;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class PlayitTcpTunnel {
    static Logger log = Logger.getLogger(PlayitTcpTunnel.class.getName());

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

        log.info("start connection to " + tunnelClaimAddress + " to claim client");
        clientBootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.warning("failed to establish connection to tunnel claim" + tunnelClaimAddress);
                disconnected();
                return;
            }

            log.info("connected to tunnel server, sending claim token");

            future.channel().writeAndFlush(Unpooled.wrappedBuffer(tunnelClaimToken)).addListener(f -> {
                if (!f.isSuccess()) {
                    log.warning("failed to send claim token");
                } else {
                    log.info("claim token sent");
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

                log.info("connection to tunnel server has been established");

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

                log.info("connecting to minecraft server at " + minecraftServerAddress);
                minecraftClient.connect().addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        log.warning("failed to connect to local minecraft server");
                        ctx.disconnect();
                        disconnected();
                        return;
                    }

                    log.info("connected to local minecraft server");

                    ByteBuf header = PlayitHeaderDecoder.encodeHeader(ctx.alloc(), trueIp);

                    if (byteBuf.readableBytes() == 0) {
                        byteBuf.release();
                        future.channel().writeAndFlush(header).addListener(f -> {
                            if (!f.isSuccess()) {
                                log.warning("failed to send playit header to minecraft server");
                                future.channel().disconnect();
                                ctx.disconnect();
                                disconnected();
                                return;
                            }

                            ctx.read();
                        });
                    } else {
                        future.channel().write(header);
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
