package com.rentalmanagement.system.dto.response;

import com.rentalmanagement.system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}
