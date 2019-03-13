package com.github.wuchao.webproject;

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
                // 用户-模块-方法-动作-操作描述
                log.debug("userLog:{}<->{}<->{}<->{}<->{}",
                        "wuc",
                        "AccountResource",
                        "createAccount",
                        "创建",
                        "创建用户基本信息");

            } else if (netInt == 2) {
                // 用户(账号)<->模块<->方法<->动作<->操作描述<->涉及大坝<->实体Class名称<->实体Json
                log.debug("userLog:test_kongh<->AccountResource(用户账户管理)<->getAccount<->浏览<->查看用户当前信息<->none<->com.aaa.bbb.service.user.dto.UserDTO<->{\"id\":1908,\"login\":\"test_xxx\",\"firstName\":\"张三\",\"lastName\":null}");

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
