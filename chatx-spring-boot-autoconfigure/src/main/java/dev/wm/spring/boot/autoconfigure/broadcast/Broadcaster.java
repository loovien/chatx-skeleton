package dev.wm.spring.boot.autoconfigure.broadcast;

import dev.wm.spring.boot.autoconfigure.domain.Payload;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/22 10:35 PM
 */
public interface Broadcaster {

  /**
   * send payload to all channel
   *
   * @param payload payload
   */
  void send(Payload payload);

  /**
   * send payload to uid collections
   *
   * @param payload payload
   * @param targets uid targets
   */
  void send(Payload payload, Integer... targets);

  /**
   * send payload use matcher
   *
   * @param payload payload
   * @param matcher matcher
   */
  void send(Payload payload, ChannelMatcher matcher);

  /**
   * obtain application channel group collections
   *
   * @return ChannelGroup
   */
  ChannelGroup getChannelGroup();
}
