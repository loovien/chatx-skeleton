package dev.wm.spring.boot.autoconfigure.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author luowen <loovien@163.com>
 * @created 1/18/2022 5:23 PM
 */
@Data
@Accessors(chain = true)
public class Payload {

    private Integer command;

    private byte[] payload;
}
