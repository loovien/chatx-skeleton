package dev.wm.spring.boot.autoconfigure.launch;

import dev.wm.spring.boot.autoconfigure.annotation.Handler;
import dev.wm.spring.boot.autoconfigure.broadcast.Broadcaster;
import dev.wm.spring.boot.autoconfigure.codec.ChatCodec;
import dev.wm.spring.boot.autoconfigure.configure.NettyProperties;
import dev.wm.spring.boot.autoconfigure.exceptions.InvalidHandlerException;
import dev.wm.spring.boot.autoconfigure.handler.AbstractHandler;
import dev.wm.spring.boot.autoconfigure.handler.CommandHandler;
import dev.wm.spring.boot.autoconfigure.handler.GatewayHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 3:49 PM
 */
@Data
@Slf4j
@Component
public class ChatApplication implements ApplicationRunner, ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

    private NioEventLoopGroup serverLoop, workerLoop;

    private final NettyProperties nettyProperties;

    private Broadcaster broadcaster;

    public ChatApplication(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.broadcaster = applicationContext.getBean(Broadcaster.class);
        this.serverLoop = new NioEventLoopGroup(nettyProperties.getServerLoopNum());
        this.workerLoop = new NioEventLoopGroup(nettyProperties.getWorkerLoopNum());
        Map<Integer, AbstractHandler> handlers = getHandlers();
        ServerBootstrap serverBootstrap = new ServerBootstrap().group(serverLoop, workerLoop)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_TIMEOUT, 10)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (pipeline == null) {
                            log.error("invalid client connect server");
                            return;
                        }
                        log.info("add channel handler");
                        pipeline.addLast(
                                new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, nettyProperties.getPacketMaxBytes(), 0, 4, -4, 0, false),
                                new ChatCodec(),
                                new GatewayHandler(handlers, broadcaster)
                        );
                    }
                }).childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);
        log.info("chatx server start at: {}, {}", nettyProperties.getAddress(), nettyProperties.getPort());
        ChannelFuture closeFuture = serverBootstrap.bind(new InetSocketAddress(nettyProperties.getAddress(), nettyProperties.getPort())).sync();
        closeFuture.channel().closeFuture().sync();
        log.info("chatx server closed, now shutdown");
        destroy();
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
