package dev.wm.spring.boot.autoconfigure.codec;

import dev.wm.spring.boot.autoconfigure.domain.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 5:20 PM
 */
public class ChatCodec extends ByteToMessageCodec<Payload> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Payload payload, ByteBuf byteBuf) throws Exception {
        byteBuf.writeIntLE(payload.getPayload().length + 8);
        byteBuf.writeIntLE(payload.getCommand());
        byteBuf.writeBytes(payload.getPayload());
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //byteBuf.resetReaderIndex();
        int length = byteBuf.getIntLE(0),
                command = byteBuf.getIntLE(4);
        byte[] bytes = new byte[length - 8];
        byteBuf.getBytes(8, bytes);
        Payload payload = new Payload().setCommand(command).setPayload(bytes);
        list.add(payload);
        byteBuf.clear();
    }
}
