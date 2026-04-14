package com.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.api_gateway.Service.RedisService;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.main.lazy-initialization=true",
		"jwt.secret=test-secret-test-secret-test-secret",
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
				"org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class ApiGatewayApplicationTests {

	@MockitoBean
	private RedisService redisService;

	@Test
	void contextLoads() {
	}

}
