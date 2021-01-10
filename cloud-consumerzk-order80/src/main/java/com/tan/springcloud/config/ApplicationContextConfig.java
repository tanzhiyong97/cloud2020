package com.tan.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    @Bean
    // @LoadBalanced保证负载均衡，不添加的话RestTemplate不能正常注入。
    @LoadBalanced
    public RestTemplate GetRestTemplate(){
        return new RestTemplate();
    }
}
