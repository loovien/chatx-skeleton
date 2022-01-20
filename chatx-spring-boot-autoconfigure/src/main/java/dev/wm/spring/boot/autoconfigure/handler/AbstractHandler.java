package dev.wm.spring.boot.autoconfigure.handler;

import dev.wm.spring.boot.autoconfigure.broadcast.Broadcaster;
import dev.wm.spring.boot.autoconfigure.constants.ChannelKey;
import dev.wm.spring.boot.autoconfigure.domain.Payload;
import dev.wm.spring.boot.autoconfigure.exceptions.NotLoginException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/2022 10:33 AM
 */
@Slf4j
public abstract class AbstractHandler implements CommandHandler {

    protected boolean authorization;

    private Broadcaster channelGroup;

    @Override
    public void handler(ChannelHandlerContext channelHandlerContext, Payload payload) {
        preHandler(channelHandlerContext, payload);
        handler(channelHandlerContext, payload, channelGroup);
    }

    /**
     * sub class implement specific logic
     *
     * @param channelHandlerContext channelHandlerContext
     * @param payload               payload
     * @param channelGroup          channelGroup
     */
    protected abstract void handler(ChannelHandlerContext channelHandlerContext, Payload payload, Broadcaster channelGroup);

    /**
     * subClass can override this method to implement complex authorization logic
     *
     * @param channelHandlerContext channelHandlerContext
     * @param payload               payload
     */
    protected void preHandler(ChannelHandlerContext channelHandlerContext, Payload payload) {
        if (!this.authorization) {
            return;
        }
        if (!channelHandlerContext.channel().hasAttr(AttributeKey.valueOf(ChannelKey.USER))) {
            log.error("socket not authorization yet, closed it, {}", channelHandlerContext.channel().remoteAddress());
            channelHandlerContext.channel().close();
            throw new NotLoginException("not authorization");
        }
    }

    @Override
    public void setLoginRequired(boolean authorization) {
        this.authorization = authorization;
    }

    @Override
    public void setChannelGroup(Broadcaster channelGroup) {
        this.channelGroup = channelGroup;
    }
}
