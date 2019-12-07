package com.github.wuchao.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;

@Profile(value = {"dev"})
@Component
@Slf4j
public class LoggingJob implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        Random random = new Random();
        int netInt;
        long startTime = System.currentTimeMillis();

        while (true) {

            netInt = random.nextInt(3);
            if (netInt == 0) {
                log.warn("logstash 采集与清洗数据到 elasticsearch 案例实战");

            } else if (netInt == 1) {
                // 用户(账号)<->用户名<->用户 IP <->模块<->方法<->操作描述
                log.debug("userLog:{}<->{}<->{}<->{}<->{}<->{}",
                        "test_001",
                        "张三",
                        "127.0.0.1",
                        "AccountResource",
                        "getAccount",
                        "查看用户当前信息");

            } else if (netInt == 2) {
                // 用户(账号)<->用户名<->用户 IP <->模块<->方法<->操作描述<->DTO Class 名称<->DTO json
                log.debug("userLog:{}<->{}<->{}<->{}<->{}<->{}<->{}<->{}",
                        "test_001",
                        "张三",
                        "127.0.0.1",
                        "AccountResource",
                        "createAccount",
                        "创建用户基本信息",
                        "com.aaa.bbb.service.user.dto.UserDTO",
                        "{\\\"login\\\":\\\"test_002\\\",\\\"firstName\\\":\\\"李四\\\",\\\"lastName\\\":null}\"");
            }

            if (System.currentTimeMillis() - startTime >= 1000 * 60 * 30) {
                // 等待 30 分钟
                Thread.sleep(1000 * 60 * 30);
                startTime = System.currentTimeMillis();
            } else {
                // 等待 10 秒
                Thread.sleep(1000 * 10);
            }

        }
    }

}
