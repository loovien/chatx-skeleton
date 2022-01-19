package dev.wm.spring.boot.autoconfigure.broadcast;

import dev.wm.spring.boot.autoconfigure.constants.ChannelKey;
import dev.wm.spring.boot.autoconfigure.domain.Payload;
import io.netty.channel.Channel;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;

import java.util.Arrays;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/22 10:04 PM
 */
@Data
@Component
@ConditionalOnProperty(prefix = "chatx.server", name = "broadcast", havingValue = "standalone")
public class Standalone extends AbstractBroadcast {

    @Override
    public void send(Payload payload) {
        channelGroup.writeAndFlush(payload);
    }

    @Override
    public void send(Payload payload, Integer... targets) {
        channelGroup.writeAndFlush(payload, channel -> {
            AttributeKey<Integer> userKey = AttributeKey.valueOf(ChannelKey.USER);
            if (!channel.hasAttr(userKey)) {
                return false;
            }
            Integer uid = channel.attr(userKey).get();
            return Arrays.stream(targets).anyMatch((v) -> v != null && v.equals(uid));
        });

    }

    @Override
    public void send(Payload payload, ChannelMatcher matcher) {
        channelGroup.writeAndFlush(payload, matcher);
    }

}
