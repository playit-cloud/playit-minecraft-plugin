package gg.playit.messages;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class SocketAddr {
    public byte[] ipBytes;
    public short portNumber;

    public InetSocketAddress toAddress() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(ipBytes), Short.toUnsignedInt(portNumber));
    }

    public void writeTo(ByteBuffer out) {
        if (ipBytes.length == 4) {
            out.put((byte) 4);
            out.put(this.ipBytes);
            out.putShort(portNumber);
        } else if (ipBytes.length == 16) {
            out.put((byte) 6);
            out.put(this.ipBytes);
            out.putShort(portNumber);
        } else {
            throw new RuntimeException("invalid ipBytes expected length of 4 or 16 but got: " + ipBytes.length);
        }
    }

    public void readFrom(ByteBuffer in) {
        var b = in.get();

        if (b == 4) {
            ipBytes = new byte[4];
        } else if (b == 6) {
            ipBytes = new byte[16];
        } else {
            throw new DecodeException("invalid socket addr id: " + (int) b);
        }

        in.get(ipBytes);
        portNumber = in.getShort();
    }

    public String ipString() {
        if (ipBytes.length == 4) {
            return String.format(
                    "%s.%s.%s.%s",
                    Byte.toUnsignedInt(ipBytes[0]), Byte.toUnsignedInt(ipBytes[1]),
                    Byte.toUnsignedInt(ipBytes[2]), Byte.toUnsignedInt(ipBytes[3])
            );
        }

        if (ipBytes.length == 16) {
            var sb = new StringBuilder();

            for (var i = 0; i < 16; ++i) {
                var b = ipBytes[i];

                if ((i % 2) == 0 && i != 0) {
                    sb.append(':');
                }

                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        }

        throw new RuntimeException("invalid ip length: " + ipBytes.length);
    }

    @Override
    public String toString() {
        if (ipBytes.length == 4) {
            return String.format(
                    "%s.%s.%s.%s:%s",
                    Byte.toUnsignedInt(ipBytes[0]), Byte.toUnsignedInt(ipBytes[1]),
                    Byte.toUnsignedInt(ipBytes[2]), Byte.toUnsignedInt(ipBytes[3]),
                    Short.toUnsignedInt(portNumber)
            );
        }
        if (ipBytes.length == 16) {
            var sb = new StringBuilder();
            sb.append('[');

            for (var i = 0; i < 16; ++i) {
                var b = ipBytes[i];

                if ((i % 2) == 0 && i != 0) {
                    sb.append(':');
                }

                sb.append(String.format("%02x", b));
            }

            sb.append("]:");
            sb.append(Short.toUnsignedInt(portNumber));

            return sb.toString();
        }

        throw new RuntimeException("invalid ip length: " + ipBytes.length);
    }
}
