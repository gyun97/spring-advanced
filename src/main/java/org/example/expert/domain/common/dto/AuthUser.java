package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.context.annotation.Configuration;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;

    public AuthUser(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }
}
