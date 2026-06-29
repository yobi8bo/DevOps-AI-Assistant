package com.example.devopsai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.devopsai.**.mapper")
public class DevopsAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevopsAiApplication.class, args);
    }
}
