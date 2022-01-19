package dev.wm.spring.boot.autoconfigure.handler;

import dev.wm.spring.boot.autoconfigure.domain.Payload;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 5:13 PM
 */
@Slf4j
@ChannelHandler.Sharable
public class GatewayHandler extends SimpleChannelInboundHandler<Payload> {

    private final ChannelGroup channelGroup;

    private final Map<Integer, AbstractHandler> handlers;

    public GatewayHandler(Map<Integer, AbstractHandler> handlers, ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
        this.handlers = handlers;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("client channel connect server, client ip: {} ", ctx.channel().remoteAddress());
        channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Payload payload) throws Exception {
        AbstractHandler handler = handlers.get(payload.getCommand());
        if (handler == null) {
            log.error("command: {} handler not found", payload.getCommand());
            channelHandlerContext.channel().close();
            return;
        }
        log.info("dispatch command: {} to handler: {}", payload.getCommand(), handler.getClass());
        handler.handler(channelHandlerContext, payload);
    }
}
