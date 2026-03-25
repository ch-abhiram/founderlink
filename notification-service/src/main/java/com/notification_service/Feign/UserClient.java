package com.notification_service.Feign;

import com.notification_service.DTO.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:9000/users")
public interface UserClient {
    
    @GetMapping("/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}
