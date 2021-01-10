# 各模块之间的联系
## 1. cloud-api-commons
工程重构：（原因：多个工程中含有相同重复的代码，比如entity下的 payment, commonResult类。）
把重复的代码提取出来，当我们在别的模块（module）中需要使用该部分公共的重复的代码时，先在模块的pom.xml文件中导入
该模块，类似于导入依赖。
```xml
        <!--导入自己定义的api通用包，可以使用payment支付entity-->
        <dependency>
            <groupId>com.tan</groupId>
            <artifactId>cloud-api-commons</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
```

## 2. cloud-consumer-order80
代表消费者。
在`application.yml`文件中，我们把它注册到Eureka中。
通过调用restTemplate实现RPC调用。（RestTemplate需要通过config，手动注册到bean容器中来）

## 3. cloud-consumerconsul-order80
代表消费者。（测试 consul作为服务发现与注册中心时，消费者远程调用provider的方法）
在`application.yml`文件中，我们把它注册到 Consul 中。
通过调用restTemplate实现RPC调用。（RestTemplate需要通过config，手动注册到bean容器中来）

## 4. cloud-consumerzk-order80
代表消费者。（测试 zookeeper 作为服务发现与注册中心时，消费者远程调用provider的方法）
在`application.yml`文件中，我们把它注册到 zookeeper 中。
通过调用restTemplate实现RPC调用。（RestTemplate需要通过config，手动注册到bean容器中来）

## 5. cloud-eureka-server7001、cloud-eureka-server7002、cloud-eureka-server7003
搭建的 Eureka-server 的集群环境。
作为 Eureka 的服务端，在pom.xml中引入的依赖是：
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
```
搭建集群环境时，分别在对面的两台Eureka服务端上注册自己。register-with-eureka 和 fetch-registry 为true的时候代表着是Eureka的客户端（Eureka-client）
```yaml
  client:
    register-with-eureka: false # 表示不向注册中心注册自己
    fetch-registry: false # false表示自己就是注册中心，我的职责就是维护服务实例，并不去检索服务
    service-url:
      defaultZone: http://eureka7002.com:7002/eureka/, http://eureka7003.com:7003/eureka/
```
注意：在启动类上要加上 @EnableEurekaServer的注解
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7001 {

    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class, args);
    }
}
```

Eureka服务端的作用是提供注册服务。

## cloud-provider-payment8001
作为 Eureka 客户端（eureka-client）把服务注册到Eureka-server中。在pom.xml文件中添加的依赖是：
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```
将自己（本module）注册到Eureka中，即 register-with-eureka 和 fetch-registry 设置为true。
default-zone有三个（因为 Eureka-server 配置了集群，需要把该服务注册到每个 服务端）
instance-id 代表注册到 eureka 后在Eureka服务端的管理页面上展示的名字。
```yaml
eureka:
  client:
    register-with-eureka: true # 是否将自己注册进EurekaServer，默认为true
    fetch-registry: true # 是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka, http://eureka7002.com:7002/eureka, http://eureka7003.com:7003/eureka
  instance:
    instance-id: payment8002
    prefer-ip-address: true
```
#### 业务代码
在该模块中编写方法，这些方法是我们想要在别的模块中调用的。所以我们需要把它注册到服务于注册中心去，这样我们在
别的模块就可以通过rpc进行调用了。

## cloud-provider-payment8002
和cloud-provider-payment8001一样。复制一份进行测试的。

## zookeeper 和 consul
都是需要先在linux服务器或者本机上开启服务。
1. zookeeper 通过 zkServer.cmd 开启服务
2. zookeeper 通过 zkCli.cmd 开启客户端管理窗口。（注意没有ui页面）

1. consul 通过 consul.exe 开启服务（最好在cmd窗口中执行该命令）
2. consul 有UI页面，替代 zookeeper的 zkCli.cmd， 默认执行完 consul.exe后会在 8500 端口下自动开启。


区别：
Eureka分为 Eureka-server 和 Eureka-client 端。
Eureka-server是集成在 springCloud 中的，不需要在服务器或者本地预先开启服务。

而Zookeeper和consul都是需要预先在服务器或者本地开启自己的服务。
所以在 application.yml 文件中，zookeeper 和 consul 都是注册服务到 注册中心的，
而 Eureka 还要区分 是server的集群还是client。

## cloud-providerconsul-payment8006
在pom.xml中添加 consul 的依赖。
```xml
        <!--SpringCloud consul-server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
```
将provider注册到服务发现与注册中心：
```yaml
  ## consul 注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        # hostname: 127.0.0.1
        service-name: ${spring.application.name}
```
## cloud-providerzk-payment8004
在pom.xml中添加 zookeeper 的依赖。(要和在本地或者服务器上开启的 zookeeper 版本一致)
```xml
        <!--SpringBoot整合Zookeeper客户端-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
            <exclusions>
                <!--先排除自带的zookeeper3.5.3-->
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <!--添加zookeeper3.6.1版本-->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.6.1</version>
        </dependency>
```
将provider注册到服务发现与注册中心：
```yaml
  cloud:
    zookeeper:
      connect-string: 127.0.0.1:2181
```
