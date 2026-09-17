package com.taskforge.scheduler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.taskforge")
@MapperScan("com.taskforge.scheduler.mapper")
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
