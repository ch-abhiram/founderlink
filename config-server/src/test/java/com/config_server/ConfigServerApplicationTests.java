package com.config_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConfigServerApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void configServerIsActive() {
        assertThat(context.containsBean("configServerConfigDataLocationResolver")).isTrue();
    }

    @Test
    void applicationNameIsSet() {
        assertThat(context.getEnvironment().getProperty("spring.application.name"))
                .isEqualTo("config-server");
    }
}
