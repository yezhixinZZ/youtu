package com.yzx.youtu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.yzx.youtu.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YouTuApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouTuApplication.class, args);
    }

}
