package dev.wm.spring.boot.autoconfigure.exceptions;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/2022 4:57 PM
 */
public class NotLoginException extends RuntimeException {
    public NotLoginException(String message) {
        super(message);
    }
}
