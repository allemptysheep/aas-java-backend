package com.aas.shinhan.core.security.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AasUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private String encodedPassword;

    @PostConstruct
    public void init() {
        // 테스트용 비밀번호 해시 생성
        this.encodedPassword = passwordEncoder.encode("admin123");
        log.info("Test user password encoded");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // TODO: 실제 DB에서 사용자 정보 조회 구현 필요
        // 현재는 테스트용 하드코딩
        if ("admin".equals(username)) {
            return new User(
                    username,
                    encodedPassword, // password: admin123
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
