package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

public class PacketDecrypter extends MessageToMessageDecoder<ByteBuf> {

    private final com.velocitypowered.natives.encryption.VelocityCipher cipher; // Paper

    public PacketDecrypter(com.velocitypowered.natives.encryption.VelocityCipher cipher) { // Paper
        this.cipher = cipher; // Paper
    }

    protected void decode(ChannelHandlerContext channelhandlercontext, ByteBuf bytebuf, List<Object> list) throws Exception {
        // Paper start
        ByteBuf compatible = com.velocitypowered.natives.util.MoreByteBufUtils.ensureCompatible(bytebuf.alloc(), this.cipher, bytebuf);
        try {
            cipher.process(compatible);
            list.add(compatible);
        } catch (Exception e) {
            compatible.release(); // compatible will never be used if we throw an exception
            throw e;
        }
        // Paper end
    }

    // Paper start
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        cipher.close();
    }
    // Paper end
}
