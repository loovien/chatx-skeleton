package dev.wm.spring.boot.autoconfigure.broadcast;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import lombok.Data;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/22 10:38 PM
 */
@Data
public abstract class AbstractBroadcast implements Broadcaster {

  protected ChannelGroup channelGroup = new DefaultChannelGroup(new DefaultEventLoop());

}
