package com.github.wuchao.webproject.controller;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class LoggingTestController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggingTestController.class);

    @GetMapping("/elk/test")
    public ResponseEntity testELK() throws InterruptedException {
        while (true) {

            Thread.sleep(10000);

            LOGGER.debug("-------------------------------");

            LOGGER.warn("Linux 内核仓库总共包含 782,487 次提交，目前有大约 19009 位开发者在维护。项目仓库大约由 61,725 个文件组成，而总共的代码行数为 25584633 行 —— 要注意还有文档，包涵诸如 Kconfig 构建文件，各种帮助程序/实用程序等这些内容。");

            LOGGER.warn("再看今年的数据，到目前为止，今年已有 49,647 次提交，增加了 2,229,836 行代码，同时删除了 2,004,759 行代码。所以净增加 225,077 行代码。");

            LOGGER.error("还值得关注的是，Linux 内核今年删除了一些对旧的 CPU 架构支持和内核中的其他代码，所以在添加了许多新功能的同时，由于进行了一些清理，内核并没有像人们预期的那样膨胀。另外，2017 年有 80,603 次提交，其中包括 3,911,061 次添加和 1,385,507 次删除。鉴于今年还剩下约四分之一的时间，所以像提交情况和代码行数这些数据目前可能会低于前两年。");

            LOGGER.error("可以看到，Linus Torvalds 依然是最活跃的提交者，拥有了 3％ 以上的占有率。而今年对内核的其他顶级贡献者也是我们熟悉的几位：David S. Miller, Arnd Bergmann, Colin Ian King, Chris Wilson 和 Christoph Hellwig. ");

            LOGGER.warn("测试异常信息打印：" + Optional.empty().get());

        }

    }

}
