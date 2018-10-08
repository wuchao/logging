## ELK 手动部署配置

### 配置 Logback
添加 pom
```
compile "net.logstash.logback:logstash-logback-encoder:5.2"
```
> 参考：[https://github.com/logstash/logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) 

配置 logback.xml
```
<?xml version="1.0" encoding="UTF-8"?>

<configuration scan="true" scanPeriod="60 seconds">

    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <timestamp key="timestamp" datePattern="yyyy-MM-dd HH:mm:ss"/>
    <property name="LOG_HOME" value="/var/log/logging"/>

    <!-- 日志导出的到本地 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/app.%d{yyyy-MM-dd}.log</fileNamePattern>
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

    <!-- 日志导的到 Logstash -->
    <!--<appender name="STASH_TCP" class="net.logstash.logback.appender.LogstashTcpSocketAppender">-->
        <!--&lt;!&ndash; 与服务器上 logstash 配置的 port 一致，且 host 为 logstash 所在服务器 &ndash;&gt;-->
        <!--<destination>localhost:4561</destination>-->
        <!--&lt;!&ndash; encoder is required &ndash;&gt;-->
        <!--<encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder"/>-->
    <!--</appender>-->

    ...
    ...
    ...

    <root level="WARN">
        <appender-ref ref="FILE"/>
        <appender-ref ref="CONSOLE"/>
    </root>

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

#### 在 Windows 端安装 logstash 提示错误 
输入 `logstash.bat -f ../config/logstash.conf --debug` 命令后出现以下错误
``` 
错误：找不到或无法加载主类 Files\Java\jdk1.7.0_80\lib;C:\Program 
```  

解决方式如下：在 logstash 安装目录中找到 `bin\logstash.bat`，打开，找到如下内容
``` 
%JAVA% %JAVA_OPTS% -cp %CLASSPATH% org.logstash.Logstash %*
```
将 %CLASSPATH% 改为 "%CLASSPATH%" 即可解决。

> 参考：[logstash 启动报无法找到主类解决方案](https://www.cnblogs.com/sbj-dawn/p/8549369.html)



### 安装 Kibana（v6.4.0）
> [Download Kibana](https://www.elastic.co/cn/downloads/kibana)  

 Kibana 解压目录下的 config/kibana.yml 配置文件中关于 ES 的配置：
```
# ES 地址
elasticsearch.url: "http://127.0.0.1:9200"
```
如果 ES 部署在其他服务器，要同步修改这里的配置

启动：运行 `bin/kibana` (or `bin\kibana.bat` on Windows)。

### Kibana 的汉化
> 参考: [https://github.com/anbai-inc/Kibana_Hanization](https://github.com/anbai-inc/Kibana_Hanization) 


### 代码测试
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
            LOGGER.warn("logstash 采集与清洗数据到 elasticsearch 案例实战");
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



### 安装 filebeat（v6.4.0）
> []()

下载、解压，进入解压后的文件夹，修改 `filebeat.xml` 配置文件。
```
#=========================== Filebeat inputs =============================

filebeat.inputs:

# Each - is an input. Most options can be set at the input level, so
# you can use different inputs for various configurations.
# Below are the input specific configurations.

- type: log

  # Change to true to enable this input configuration.
  enabled: true

  # Paths that should be crawled and fetched. Glob based paths.
  paths:
    # 在服务器部署时这样写没有获取到日志，写成 \* 这种形式就可以
    - D:\var\log\logging\app.*.log
    # 可以获取其它目录下的日志，写法如下
    #- c:\programdata\elasticsearch\logs\*


#============================= Filebeat modules ===============================

filebeat.config.modules:
  # Glob pattern for configuration loading
  path: ${path.config}/modules.d/*.yml

  # Set to true to enable config reloading
  reload.enabled: true

  # Period on which files under path should be checked for changes
  #reload.period: 10s


#============================= Redis output ===================================
output.redis:
  hosts: ["127.0.0.1"]
  port: 6379
  db: 2
  index: "app-staging"
```
> filebeat 和 web 项目放在一个服务器上。

打开 cmd，切换到 filebeat 解压目录，输入 `filebeat.exe`，回车即可。

启动后 filebeat 就可以从应用服务器获取日志，保存到到 Redis，保存到 Redis 中的 key 即为配置的 “app-staging”，value 是一个 list 队列。
> 使用以上配置的问题是 Redis 保存的日志一直增加，不会过期 参考：[redis数据库队列（list），集合（set）元素设置类似过期（expire）功能](https://blog.csdn.net/leean950806/article/details/78669070)。所以改用 kafka，Redis 也有类似 kafka 的订阅功能，但是对于大量日志的传输，还是考虑使用 kafka。

修改 logstash 的配置。
```
input {

  redis {
    data_type => "list"
    key => "app-staging"
    host => "localhost"
    port => 6379
    db => 2
  }

}

output {
  # 从 Redis 获取数据传输到 ES
  elasticsearch {
    hosts => ["127.0.0.1:9200"]
    index => "app-staging-log-%{+YYYY.MM.dd}"
  }

}
```
> 参考：使用 filebeat 获取日志，Redis 做缓存队列（[ELK之filebeat详解](https://www.ixdba.net/archives/2018/01/1111.htm)）


### 安装 Kafka（用 Kafka 替换 Redis 做缓冲队列）
> [Quickstart](https://kafka.apache.org/quickstart)
>
> Windows 脚本在 bin/windows 目录下。

kafka 配置文件（./config/server.properties）说明
```
listeners=PLAINTEXT://127.0.0.1:9092

# kafka 存放数据的路径。这个路径并不是唯一的，可以是多个，路径之间只需要使用逗号分隔即可；
# 每当创建新 partition 时，都会选择在包含最少 partitions 的路径下进行。
log.dirs=E://Kafka//logs

# 数据存储的最大时间，超过这个时间会根据 log.cleanup.policy 设置的策略处理数据
log.retention.hours=168

# Zookeeper connection string (see zookeeper docs for details).
# This is a comma separated host:port pairs, each corresponding to a zk
# server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002".
# You can also append an optional chroot string to the urls to specify the
# root directory for all kafka znodes.
zookeeper.connect=localhost:2181
```

以上演示，ZooKeeper 和 Kafka 都只部署了一个节点。


#### 在 Windows 端安装 kafka 提示错误 
输入 `kafka-server-start.bat ../../config/server.properties` 命令后出现以下错误
``` 
错误：找不到或无法加载主类 Files\Java\jdk1.7.0_80\lib;C:\Program 
```  

解决方式如下：在 kafka 安装目录中找到 bin\windows 目录中的 `kafka-run-class.bat` ，打开，找到如下内容
``` 
set COMMAND=%JAVA% %KAFKA_HEAP_OPTS% %KAFKA_JVM_PERFORMANCE_OPTS% %KAFKA_JMX_OPTS% %KAFKA_LOG4J_OPTS% -cp %CLASSPATH% %KAFKA_OPTS% %*
```
为 %CLASSPATH% 加上双引号，
``` 
set COMMAND=%JAVA% %KAFKA_HEAP_OPTS% %KAFKA_JVM_PERFORMANCE_OPTS% %KAFKA_JMX_OPTS% %KAFKA_LOG4J_OPTS% -cp "%CLASSPATH%" %KAFKA_OPTS% %*
``` 

> 参考：[在Windows端安装kafka 提示错误: 找不到或无法加载主类 的解决方案](https://blog.csdn.net/u012931508/article/details/55211390)


配置 filebeat.yml：
```
output.kafka:
  enabled: true
  hosts: ["127.0.0.1:9092"]
  topic: "test"
  partition.hash:
    reachable_only: true
  compression: gzip
  max_message_bytes: 1000000
  required_acks: 1
```

配置 logstash.config：
```
input {

  kafka {
    bootstrap_servers => "127.0.0.1:9092"
    topics => "test"
    consumer_threads => 1
    decorate_events => true
    codec => "json"
    auto_offset_reset => "latest"
  }

}

output {

  elasticsearch {
    hosts => ["127.0.0.1:9200"]
    index => "app-staging-log-%{+YYYY.MM.dd}"
  }

}
```

多个 input 和 多个 output 的配置写法：
```
input {

  tcp {
    host => "127.0.0.1"
    port => 4561
    codec => "json"
    type => "dam-staging-tcp"
  }

  kafka {
    bootstrap_servers => "127.0.0.1:9092"
    topics => "app-staging"
    consumer_threads => 1
    decorate_events => true
    codec => "json"
    auto_offset_reset => "latest"
    type => "app-staging-kafka"
  }

  kafka {
    bootstrap_servers => "127.0.0.1:9092"
    topics => "app-prod"
    consumer_threads => 1
    decorate_events => true
    codec => "json"
    auto_offset_reset => "latest"
    type => "app-prod-kafka"
  }

  redis {
    host => "127.0.0.1"
    port => 6379
    password => "app-redis"
    db => 2
    data_type => "list"
    key => "app-staging"
    type => "app-staging-redis"
  }

}

filter {
  grok {
    match => {
      "message" => [
        "%{DATA:logType}\:%{GREEDYDATA:user}<->%{GREEDYDATA:module}<->%{GREEDYDATA:method}<->%{GREEDYDATA:action}<->%{GREEDYDATA:description}<->%{GREEDYDATA:json}",
        "%{DATA:logType}\:%{GREEDYDATA:user}<->%{GREEDYDATA:moudle}<->%{GREEDYDATA:method}<->%{GREEDYDATA:action}<->%{GREEDYDATA:description}"
      ]
    }
    remove_field => "message"
  }
}

output {

  if [type] == "dam-staging-tcp" {
    elasticsearch {
      hosts => ["127.0.0.1:9200"]
      // ElasticSearch 索引形式
      index => "app-staging-tcp-%{+YYYY.MM.dd}"
    }
  }

  if [type] == "app-staging-kafka" {
    elasticsearch {
      hosts => ["127.0.0.1:9200"]
      index => "app-staging-log-%{+YYYY.MM.dd}"
    }
  }

  if [type] == "app-prod-kafka" {
    elasticsearch {
      hosts => ["127.0.0.1:9200"]
      index => "app-prod-log-%{+YYYY.MM.dd}"
    }
  }

  if [type] == "app-staging-redis" {
    elasticsearch {
      hosts => ["127.0.0.1:9200"]
      index => "app-staging-log-%{+YYYY.MM.dd}"
    }
  }

}
```
> [logstash grok 分割匹配日志](https://www.cnblogs.com/shantu/p/4598875.html)

启动 logstash 如果报如下错：
```
logstash.config.sourceloader] No configuration found in the configured sources.
```
则修改 `D:\ELK\logstash-6.4.0-kafka\config` 目录下的 `pipelines.yml` 配置文件如下：
```
- pipeline.id: another_test
    path.config: "D:\ELK\logstash-6.4.0-kafka\config\logstash.config"
```
重启 logstash 即可。
> 参考：[Multiple Pipelines doesn't seem to work with Windows OS](https://github.com/elastic/logstash/issues/9144)


## ELK Docker 部署配置
