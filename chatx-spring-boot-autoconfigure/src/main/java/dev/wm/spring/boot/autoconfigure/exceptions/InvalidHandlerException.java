package dev.wm.spring.boot.autoconfigure.exceptions;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/2022 10:22 AM
 */
public class InvalidHandlerException extends RuntimeException {
    public InvalidHandlerException(String s) {
        super(s);
    }
}
