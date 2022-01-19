package dev.wm.spring.boot.autoconfigure.annotation;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * @author luowen <loovien@163.com>
 * @created 1/19/2022 9:34 AM
 */
@Inherited
@Configuration
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {

    boolean auth() default true;

    int command() default 0;
}
