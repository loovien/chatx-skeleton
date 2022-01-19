package dev.wm.spring.boot.autoconfigure.handler;

import dev.wm.spring.boot.autoconfigure.domain.Payload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/2022 10:11 AM
 */
public interface CommandHandler {


    /**
     * command handler processor
     *
     * @param channelHandlerContext channel
     * @param payload               payload
     */
    void handler(ChannelHandlerContext channelHandlerContext, Payload payload);

    /**
     * command handler need login or not
     *
     * @param authorization authorization
     */
    void setLoginRequired(boolean authorization);

    /**
     * inject channel group int command handler
     *
     * @param channelGroup channelGroup
     */
    void setChannelGroup(ChannelGroup channelGroup);


}
