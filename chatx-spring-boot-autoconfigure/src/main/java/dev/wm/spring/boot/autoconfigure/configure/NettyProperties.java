package dev.wm.spring.boot.autoconfigure.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 3:34 PM
 */
@Data
@ConfigurationProperties(prefix = "chatx.server")
public class NettyProperties {

    private String address;

    private Integer port;

    private Integer serverLoopNum;

    private Integer workerLoopNum;

    private Integer packetMaxBytes;

    private String broadcast;

}
