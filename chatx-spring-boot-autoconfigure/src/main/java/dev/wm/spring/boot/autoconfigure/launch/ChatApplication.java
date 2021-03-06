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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "chatx.server", name = "mode", havingValue = "tcp")
public class ChatApplication extends AbstractApplication {

    public ChatApplication(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.broadcaster = applicationContext.getBean(Broadcaster.class);
        this.serverLoop = new NioEventLoopGroup(nettyProperties.getServerLoopNum() + 1);
        this.workerLoop = new NioEventLoopGroup(nettyProperties.getWorkerLoopNum());
        Map<Integer, AbstractHandler> handlers = getHandlers();
        this.serverLoop.execute(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(serverLoop, workerLoop)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .option(ChannelOption.SO_BACKLOG, nettyProperties.getOptionSoBacklog())
                    //.childOption(ChannelOption.SO_TIMEOUT, 10)
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
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, nettyProperties.getOptionSoKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, nettyProperties.getOptionTcpNoDelay());
            log.info("chatx server start at: {}, {}", nettyProperties.getAddress(), nettyProperties.getPort());
            try {
                ChannelFuture closeFuture = serverBootstrap.bind(new InetSocketAddress(nettyProperties.getAddress(), nettyProperties.getPort())).sync();
                closeFuture.channel().closeFuture().sync();
                destroy();
            } catch (Exception e) {
                log.error("server occur some exception: ", e);
            }
            log.info("server closed, now shutdown");
        });
    }


}
