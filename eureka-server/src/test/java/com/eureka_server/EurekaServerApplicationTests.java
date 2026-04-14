package com.eureka_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.main.lazy-initialization=true",
        "spring.autoconfigure.exclude=org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration"
})
class EurekaServerApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void applicationStartsWithExpectedConfiguration() {
        assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);
    }

    @Test
    void applicationNameIsSet() {
        assertThat(context.getEnvironment().getProperty("spring.application.name"))
                .isEqualTo("eureka-server");
    }
}
