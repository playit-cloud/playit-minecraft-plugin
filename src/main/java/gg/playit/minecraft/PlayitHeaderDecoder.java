package gg.playit.minecraft;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PlayitHeaderDecoder extends ByteToMessageDecoder {
    static final Logger log = Logger.getLogger(PlayitHeaderDecoder.class.getName());

    private static final byte[] MAGIC = {'P', 'L', 'A', 'Y', 'I', 'T'};

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        SocketAddress remote = ctx.channel().remoteAddress();
        if (!isLocalhost(remote)) {
            ctx.pipeline().remove(this);
            return;
        }

        if (in.readableBytes() < MAGIC.length) {
            return;
        }

        for (int i = 0; i < MAGIC.length; i++) {
            if (in.getByte(in.readerIndex() + i) != MAGIC[i]) {
                ctx.pipeline().remove(this);
                return;
            }
        }

        if (in.readableBytes() < MAGIC.length + 1) {
            return;
        }

        int family = in.getByte(in.readerIndex() + MAGIC.length) & 0xFF;
        int ipLen;
        if (family == 4) {
            ipLen = 4;
        } else if (family == 6) {
            ipLen = 16;
        } else {
            ctx.pipeline().remove(this);
            return;
        }

        int headerSize = MAGIC.length + 1 + ipLen + 2;
        if (in.readableBytes() < headerSize) {
            return;
        }

        in.skipBytes(MAGIC.length);
        in.skipBytes(1); // family byte already read

        byte[] ipBytes = new byte[ipLen];
        in.readBytes(ipBytes);
        int port = in.readUnsignedShort();

        InetAddress addr = InetAddress.getByAddress(ipBytes);
        InetSocketAddress realAddress = new InetSocketAddress(addr, port);

        log.info("parsed playit header, true client IP: " + realAddress);
        updateConnectionAddress(ctx, realAddress);

        ctx.pipeline().remove(this);
    }

    /**
     * Walk the pipeline looking for the Minecraft NetworkManager/Connection handler
     * and set its SocketAddress field to the real client address.
     */
    private void updateConnectionAddress(ChannelHandlerContext ctx, InetSocketAddress realAddress) {
        for (Map.Entry<String, ChannelHandler> entry : ctx.pipeline()) {
            ChannelHandler handler = entry.getValue();
            if (handler == this) {
                continue;
            }
            if (trySetAddress(handler, realAddress)) {
                log.info("updated connection address on handler: " + entry.getKey());
                return;
            }
        }
        log.warning("could not find NetworkManager handler to update address");
    }

    private boolean trySetAddress(Object target, SocketAddress address) {
        Class<?> clazz = target.getClass();

        try {
            Field field = searchField(clazz, "address");
            if (field != null && SocketAddress.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                field.set(target, address);
                return true;
            }
        } catch (Exception ignored) {
        }

        Field candidate = null;
        int count = 0;

        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (SocketAddress.class.isAssignableFrom(f.getType())) {
                    candidate = f;
                    count++;
                }
            }
        }

        if (count == 1 && candidate != null) {
            try {
                candidate.setAccessible(true);
                candidate.set(target, address);
                return true;
            } catch (Exception e) {
                log.warning("failed to set SocketAddress field on " + clazz.getName() + ": " + e);
            }
        }

        return false;
    }

    private static Field searchField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static boolean isLocalhost(SocketAddress addr) {
        if (!(addr instanceof InetSocketAddress inet)) {
            return false;
        }
        InetAddress ip = inet.getAddress();
        return ip.isLoopbackAddress();
    }

    /**
     * Encode the playit header for the given real client address.
     */
    public static ByteBuf encodeHeader(ByteBufAllocator alloc, InetSocketAddress realAddress) {
        byte[] ipBytes = realAddress.getAddress().getAddress();
        int family = ipBytes.length == 4 ? 4 : 6;
        int headerSize = MAGIC.length + 1 + ipBytes.length + 2;

        ByteBuf buf = alloc.buffer(headerSize, headerSize);
        buf.writeBytes(MAGIC);
        buf.writeByte(family);
        buf.writeBytes(ipBytes);
        buf.writeShort(realAddress.getPort());
        return buf;
    }
}
