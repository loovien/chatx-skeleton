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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
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
@ConditionalOnProperty(prefix = "chatx.server", name = "mode", havingValue = "websocket")
public class WebsocketChatApplication extends AbstractApplication {

    public WebsocketChatApplication(NettyProperties nettyProperties) {
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
                            pipeline.addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(nettyProperties.getPacketMaxBytes()),
                                    new WebSocketServerCompressionHandler(),
                                    new WebSocketServerProtocolHandler(nettyProperties.getWebsocketPath(), null, true),
                                    new GatewayHandler(handlers, broadcaster)
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, nettyProperties.getOptionSoKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, nettyProperties.getOptionTcpNoDelay());
            log.info("chatx websocket server start at: {}, {}", nettyProperties.getAddress(), nettyProperties.getPort());
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
