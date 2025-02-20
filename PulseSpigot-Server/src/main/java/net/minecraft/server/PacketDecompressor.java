package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;

public class PacketDecompressor extends ByteToMessageDecoder {

    // Paper start
    private final Inflater inflater;
    private final com.velocitypowered.natives.compression.VelocityCompressor compressor;
    private int threshold;
    // Paper end

    public PacketDecompressor(int i) {
    // Paper start
        this(null, i);
    }

    public PacketDecompressor(com.velocitypowered.natives.compression.VelocityCompressor compressor, int compressionThreshold) {
        this.threshold = compressionThreshold;
        this.inflater = compressor == null ? new Inflater() : null;
        this.compressor = compressor;
    }
    // Paper end

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) throws Exception {
        if (bytebuf.readableBytes() != 0) {
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);
            int i = packetdataserializer.e();

            if (i == 0) {
                list.add(packetdataserializer.readBytes(packetdataserializer.readableBytes()));
            } else {
                // Paper start
                if (i < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                // Paper end
                }

                if (i > 2097152) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                }

                // Paper start
                if (this.inflater != null) {
                    byte[] abyte = new byte[packetdataserializer.readableBytes()];

                    packetdataserializer.readBytes(abyte);
                    this.inflater.setInput(abyte);
                    byte[] abyte1 = new byte[i];
                    this.inflater.inflate(abyte1);
                    list.add(Unpooled.wrappedBuffer(abyte1));
                    this.inflater.reset();
                    return;
                }

                ByteBuf compatibleIn = com.velocitypowered.natives.util.MoreByteBufUtils.ensureCompatible(channelhandlercontext.alloc(), this.compressor, bytebuf);
                ByteBuf uncompressed = com.velocitypowered.natives.util.MoreByteBufUtils.preferredBuffer(channelhandlercontext.alloc(), this.compressor, i);
                try {
                    this.compressor.inflate(compatibleIn, uncompressed, i);
                    list.add(uncompressed);
                    bytebuf.clear();
                } catch (Exception e) {
                    uncompressed.release();
                    throw e;
                } finally {
                    compatibleIn.release();
                }
                // Paper end
            }

        }
    }

    // Paper start
    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        if (this.compressor != null) {
            this.compressor.close();
        }
    }
    // Paper end

    public void a(int i) {
        this.threshold = i; // Paper
    }
}
