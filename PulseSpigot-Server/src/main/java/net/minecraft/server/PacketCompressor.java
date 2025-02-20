package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf> {

    // Paper start
    private final byte[] encodeBuf;
    private final Deflater deflater;
    private final com.velocitypowered.natives.compression.VelocityCompressor compressor;
    private int threshold;
    // Paper end

    public PacketCompressor(int i) {
        // Paper start
        this(null, i);
    }
    public PacketCompressor(com.velocitypowered.natives.compression.VelocityCompressor compressor, int compressionThreshold) {
        this.threshold = compressionThreshold;
        if (compressor == null) {
            this.encodeBuf = new byte[8192];
            this.deflater = new Deflater();
        } else {
            this.encodeBuf = null;
            this.deflater = null;
        }
        this.compressor = compressor;
        // Paper end
    }

    protected void encode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, ByteBuf bytebuf1) throws Exception { // Paper
        int i = bytebuf.readableBytes();
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf1);

        if (i < this.threshold) { // Paper
            packetdataserializer.b(0);
            packetdataserializer.writeBytes(bytebuf);
        } else {
            // Paper start
            if (this.deflater != null) {
                byte[] abyte = new byte[i];

                bytebuf.readBytes(abyte);
                packetdataserializer.b(abyte.length);
                this.deflater.setInput(abyte, 0, i);
                this.deflater.finish();

                while (!this.deflater.finished()) {
                    int j = this.deflater.deflate(this.encodeBuf);

                    packetdataserializer.writeBytes(this.encodeBuf, 0, j);
                }

                this.deflater.reset();
                return;
            }
            // Paper end

            // Paper start
            packetdataserializer.b(i);
            ByteBuf compatibileIn = com.velocitypowered.natives.util.MoreByteBufUtils.ensureCompatible(channelhandlercontext.alloc(), this.compressor, bytebuf);
            try {
                this.compressor.deflate(compatibileIn, bytebuf1);
            } finally {
                compatibileIn.release();
            }
            // Paper end
        }

    }

    // Paper start
    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception{
        if (this.compressor != null) {
            // We allocate bytes to be compressed plus 1 byte. This covers two cases:
            //
            // - Compression
            //    According to https://github.com/ebiggers/libdeflate/blob/master/libdeflate.h#L103,
            //    if the data compresses well (and we do not have some pathological case) then the maximum
            //    size the compressed size will ever be is the input size minus one.
            // - Uncompressed
            //    This is fairly obvious - we will then have one more than the uncompressed size.
            int initialBufferSize = msg.readableBytes() + 1;
            return com.velocitypowered.natives.util.MoreByteBufUtils.preferredBuffer(ctx.alloc(), this.compressor, initialBufferSize);
        }

        return super.allocateBuffer(ctx, msg, preferDirect);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (this.compressor != null) {
            this.compressor.close();
        }
    }
    // Paper end

    // Paper start - OBFHELPER
    public void setThreshold(int threshold) { this.a(threshold); }
    public void a(int i) {
        this.threshold = i;
    // Paper end
    }

}
