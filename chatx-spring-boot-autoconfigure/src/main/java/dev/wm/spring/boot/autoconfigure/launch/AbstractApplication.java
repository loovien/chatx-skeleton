package dev.wm.spring.boot.autoconfigure.launch;

import dev.wm.spring.boot.autoconfigure.annotation.Handler;
import dev.wm.spring.boot.autoconfigure.broadcast.Broadcaster;
import dev.wm.spring.boot.autoconfigure.configure.NettyProperties;
import dev.wm.spring.boot.autoconfigure.exceptions.InvalidHandlerException;
import dev.wm.spring.boot.autoconfigure.handler.AbstractHandler;
import dev.wm.spring.boot.autoconfigure.handler.CommandHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luowen <loovien@163.com>
 * @created 1/20/2022 6:05 PM
 */
@Slf4j
public abstract class AbstractApplication implements ApplicationRunner, ApplicationContextAware, DisposableBean {
    protected ApplicationContext applicationContext;

    protected NioEventLoopGroup serverLoop, workerLoop;

    protected NettyProperties nettyProperties;

    protected Broadcaster broadcaster;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        log.info("chatx server shutdown now");
        this.serverLoop.shutdownGracefully();
        this.workerLoop.shutdownGracefully();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractHandler> Map<Integer, T> getHandlers() {
        Map<Integer, T> respResult = new HashMap<>();
        Map<String, CommandHandler> handlers = this.applicationContext.getBeansOfType(CommandHandler.class);
        for (CommandHandler command : handlers.values()) {
            Handler annotation = command.getClass().getAnnotation(Handler.class);
            if (annotation == null) {
                throw new InvalidHandlerException("command handler must with Handler annotation");
            }
            command.setChannelGroup(broadcaster);
            command.setLoginRequired(annotation.auth());
            respResult.put(annotation.command(), (T) command);
        }
        return respResult;
    }


}
