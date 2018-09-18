

### 配置 Logback
添加 pom
```
compile "net.logstash.logback:logstash-logback-encoder:5.2"
```

配置 logback.xml
```
<?xml version="1.0" encoding="UTF-8"?>

<!--<configuration scan="true">-->
<configuration scan="true" scanPeriod="10 seconds">

    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <timestamp key="timestamp" datePattern="yyyy-MM-dd HH:mm:ss"/>
    <property name="LOG_HOME" value="/var/log/web-project"/>

    <!--The FILE and ASYNC appenders are here as examples for a production configuration-->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/news/news.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <encoder>
            <charset>utf-8</charset>
            <Pattern>%d %-5level [%thread] %logger{0}: %msg%n</Pattern>
        </encoder>
        <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
            <MaxFileSize>100MB</MaxFileSize>
        </triggeringPolicy>
    </appender>

    <!--<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">-->
    <!--<queueSize>512</queueSize>-->
    <!--<appender-ref ref="FILE"/>-->
    <!--</appender>-->

    <!--日志导出的到 Logstash-->
    <appender name="TCP_STASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>127.0.0.1:4561</destination>
        <!-- encoder is required -->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        <!--<connectionStrategy>-->
        <!--<roundRobin>-->
        <!--<connectionTTL>0.5 minutes</connectionTTL>-->
        <!--</roundRobin>-->
        <!--</connectionStrategy>-->
        <!-- Enable SSL using the JVM's default keystore/truststore -->
        <!--<ssl/>-->
        <writeBufferSize>81920</writeBufferSize>
        <!--<keepAliveDuration>5 minutes</keepAliveDuration>
        <reconnectionDelay>1 second</reconnectionDelay>-->
    </appender>

    <logger name="javax.activation" level="WARN"/>
    <logger name="javax.mail" level="WARN"/>
    <logger name="javax.xml.bind" level="WARN"/>
    <logger name="ch.qos.logback" level="WARN"/>
    <logger name="com.codahale.metrics" level="WARN"/>
    <logger name="com.ryantenney" level="WARN"/>
    <logger name="com.sun" level="WARN"/>
    <logger name="com.zaxxer" level="WARN"/>
    <logger name="io.undertow" level="WARN"/>
    <logger name="io.undertow.websockets.jsr" level="ERROR"/>
    <logger name="org.ehcache" level="WARN"/>
    <logger name="org.apache" level="WARN"/>
    <logger name="org.apache.catalina.startup.DigesterFactory" level="OFF"/>
    <logger name="org.bson" level="WARN"/>
    <logger name="org.elasticsearch" level="WARN"/>
    <logger name="org.hibernate.validator" level="WARN"/>
    <logger name="org.hibernate" level="WARN"/>
    <logger name="org.hibernate.ejb.HibernatePersistence" level="OFF"/>
    <logger name="org.springframework" level="WARN"/>
    <logger name="org.springframework.web" level="DEBUG"/>
    <logger name="org.springframework.security" level="WARN"/>
    <logger name="org.springframework.cache" level="WARN"/>
    <logger name="org.thymeleaf" level="WARN"/>
    <logger name="org.xnio" level="WARN"/>
    <logger name="springfox" level="WARN"/>
    <logger name="sun.rmi" level="WARN"/>
    <logger name="liquibase" level="WARN"/>
    <logger name="LiquibaseSchemaResolver" level="WARN"/>
    <logger name="sun.rmi.transport" level="WARN"/>

    <springProfile name="dev">
        <logger name="com.github.wuchao.webproject.controller" level="DEBUG"/>
        <root level="WARN">
            <appender-ref ref="FILE"/>
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <logger name="com.github.wuchao.webproject.controller" level="WARN"/>
        <root level="WARN">
            <appender-ref ref="TCP_STASH"/>
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <contextListener class="ch.qos.logback.classic.jul.LevelChangePropagator">
        <resetJUL>true</resetJUL>
    </contextListener>

</configuration>
```

### 安装 ElasticSearch（v6.4.0）
> [Download Elasticsearch](https://www.elastic.co/cn/downloads/elasticsearch)

启动：运行 `bin/elasticsearch` (or `bin\elasticsearch.bat` on Windows)。

浏览器打开 http://127.0.0.1:9200， 显示如下内容，说明启动成功。
```
{
  "name" : "Tgf8YPO",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "61LwqYhdQnaWU_zMoZ3BIw",
  "version" : {
    "number" : "6.4.0",
    "build_flavor" : "default",
    "build_type" : "zip",
    "build_hash" : "595516e",
    "build_date" : "2018-08-17T23:18:47.308994Z",
    "build_snapshot" : false,
    "lucene_version" : "7.4.0",
    "minimum_wire_compatibility_version" : "5.6.0",
    "minimum_index_compatibility_version" : "5.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

查看 elasticsearch 集群状态信息： `http://127.0.0.1:9200/_cluster/health?pretty=true` 。
```
{
  "cluster_name" : "elasticsearch",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 1,
  "active_shards" : 1,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 100.0
}
```

查看 elasticsearch 索引信息： `http://127.0.0.1:9200/_cat/indices?v`。


### 安装 Logstash（v6.4.0）
> [Download Logstash](https://www.elastic.co/cn/downloads/logstash)

打开 `config/logstash.yml` 配置文件，修改 host 和 port 。
```
http.host: "127.0.0.1"
http.port: 9601
```

在 logstash 的 config 目录下新建配置文件，文件名命名为 `logstash.conf`，配置内容如下。
```
input {

  tcp {
      # host 和 port 要和 logback.xml 中的 destination 配置保持一致
      host => "127.0.0.1"
      port => 4560
      codec => json_lines
      type => "app-dev"
  }

  tcp {
      host => "127.0.0.1"
      port => 4561
      codec => json_lines
      type => "app-staging"
  }

}

output {

  elasticsearch {
    hosts => ["localhost:9200"]
    index => "%{[type]}-log-%{+YYYY.MM.dd}"
  }


  if "_jsonparsefailure" in [tags] {
		file {
			path => "D:\var\log\_jsonparsefailure.txt"
		}
	}

  stdout { codec => rubydebug }

}
```

启动 logstash。
```
logstash.bat -f ../config/logstash.conf --debug
```

查看 logstash 启动信息：浏览器打开 `http://127.0.0.1:9601/?pretty=true` 。
```
{
  "host" : "PC180495",
  "version" : "6.4.0",
  "http_address" : "127.0.0.1:9601",
  "id" : "ccc983f2-a650-4892-adb8-d6622918a771",
  "name" : "PC180495",
  "build_date" : "2018-08-18T00:25:22Z",
  "build_sha" : "f8014ac54e6c8ff6c071c0960ca1b00e9735f43a",
  "build_snapshot" : false
}
```


## 安装 Kibana（v6.4.0）
> [Download Kibana](https://www.elastic.co/cn/downloads/kibana)  

 Kibana 解压目录下的 config/kibana.yml 配置文件中关于 ES 的配置：
```
# ES 地址
elasticsearch.url: "http://127.0.0.1:9200"
```
如果 ES 部署在其他服务器，要同步修改这里的配置

启动：运行 `bin/kibana` (or `bin\kibana.bat` on Windows)。


## 代码测试
```
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

            LOGGER.warn("Linux 内核仓库总共包含 782,487 次提交，目前有大约 19009 位开发者在维护。项目仓库大约由 61,725 个文件组成，而总共的代码行数为 25584633 行 —— 要注意还有文档，包涵诸如 Kconfig 构建文件，各种帮助程序/实用程序等这些内容。");

            LOGGER.warn("再看今年的数据，到目前为止，今年已有 49,647 次提交，增加了 2,229,836 行代码，同时删除了 2,004,759 行代码。所以净增加 225,077 行代码。");

            LOGGER.error("还值得关注的是，Linux 内核今年删除了一些对旧的 CPU 架构支持和内核中的其他代码，所以在添加了许多新功能的同时，由于进行了一些清理，内核并没有像人们预期的那样膨胀。另外，2017 年有 80,603 次提交，其中包括 3,911,061 次添加和 1,385,507 次删除。鉴于今年还剩下约四分之一的时间，所以像提交情况和代码行数这些数据目前可能会低于前两年。");

            LOGGER.error("可以看到，Linus Torvalds 依然是最活跃的提交者，拥有了 3％ 以上的占有率。而今年对内核的其他顶级贡献者也是我们熟悉的几位：David S. Miller, Arnd Bergmann, Colin Ian King, Chris Wilson 和 Christoph Hellwig. ");

            LOGGER.warn("测试异常信息打印：" + Optional.empty().get());

        }

    }

}
```

在 debug 模式启动的 logstash 的控制台可以看到输出的信息，这时浏览器打开 localhost:5601 打开 Kibana 查看日志 。

先进入 Management -> Kibana -> Create index pattern，如下图所示。

![](./images/create-index-pattern-01.jpg)

输入能够匹配已有的索引名称的索引正则，如“app-staging*”,点击下一步。

![](./images/create-index-pattern-02.jpg)

选择 `@Timestamp`，点击下一步。

回到首页，如果项目中有日志输出，即可看到日志。首页右上角可以设置查看某一时间范围内的日志，还可以设置日志刷新频率等。

![](./images/log-list.jpg)

点击每条日志左上角的三角形按钮可以查看日志的详细信息。

![](./images/log-detail.jpg)

日志列表左边的筛选处还可以控制显示其他日志和日志列表显示的属性。

![](./images/log-filter-menu.jpg)
