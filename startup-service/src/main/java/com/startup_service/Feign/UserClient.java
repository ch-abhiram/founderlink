package com.startup_service.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.startup_service.DTO.UserDto;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

	@GetMapping("/users/{email:.+}")
	UserDto getUser(@PathVariable("email") String email);
}
