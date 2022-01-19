package dev.wm.spring.boot.autoconfigure;

import dev.wm.spring.boot.autoconfigure.configure.NettyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 2:09 PM
 */
@ComponentScan
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NettyProperties.class)
public class ChatAutoConfigure {
}
