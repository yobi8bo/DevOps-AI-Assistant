package com.example.devopsai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * DevopsAiApplication类，负责承载对应业务能力。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@SpringBootApplication
@MapperScan("com.example.devopsai.**.mapper")
public class DevopsAiApplication {
    /**
     * 执行main处理逻辑。
     * @param args args参数。
     */

    public static void main(String[] args) {
        SpringApplication.run(DevopsAiApplication.class, args);
    }
}
