package com.team_service.Feign;

import com.team_service.DTO.UserDto;
import com.team_service.DTO.UpdateRoleRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USER-SERVICE", path = "/users")
public interface UserClient {

    @GetMapping("/{email:.+}")
    UserDto getUserByEmail(@PathVariable("email") String email);

    @PutMapping("/{email:.+}/role")
    UserDto updateUserRole(
            @PathVariable("email") String email,
            @RequestBody UpdateRoleRequest request,
            @RequestHeader("X-User-Email") String actingEmail,
            @RequestHeader("X-User-Role") String actingRole);
}
