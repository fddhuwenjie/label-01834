package com.mahjong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mahjong.mapper")
public class MahjongApplication {
    public static void main(String[] args) {
        SpringApplication.run(MahjongApplication.class, args);
    }
}
