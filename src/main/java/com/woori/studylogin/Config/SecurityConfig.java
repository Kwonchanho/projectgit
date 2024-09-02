package com.woori.studylogin.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //로그인완성 후 각 맵핑별로 권한부여(Controller에 맵핑 접근제한)
        http.authorizeHttpRequests((auth)->{
            auth.requestMatchers("/test1", "/test2").permitAll(); //전체사용
            auth.requestMatchers("/user/update", "/board/new", "/board/update", "/board/delete", "/board/like","/comment/insert").authenticated(); //로그인 후
            auth.requestMatchers("/user/register").anonymous(); //로그인 전
            auth.requestMatchers("/images/**").permitAll();
            //상품 등록/수정/삭제는 운영자와 관리자만 접근이 가능하게
            auth.requestMatchers("/product/new", "/product/edit", "/product/delete").hasAnyRole("admin", "master");
            auth.requestMatchers("/company/new", "/company/edit").hasRole("master");
            auth.requestMatchers("/company/view").hasAnyRole("admin", "master");
            auth.requestMatchers("/company/register", "/company/edit", "/company/remove").hasAnyRole("admin", "master");
            auth.anyRequest().permitAll();//지정되지 않는 맵핑은 모두 허용
        });
        //permitAll(모두), anonymouse(로그인안된상태), anthenticated(로그인상태), hasRole(1개의 대상), hasAnyRole(여러대상)

        //로그인 연결 맵핑
        http.formLogin(login -> login
                .defaultSuccessUrl("/", true) //성공시 이동할 페이지
                .loginPage("/login") //맵핑명
                .usernameParameter("username") //로그인폼에 아이디변수명
                .permitAll() //모든 사용자는 로그인폼을 사용가능
                //성공시처리할 이벤트(사용자가 작성한 이벤트-섹션에 변수저장)
                .successHandler(customAuthenticationSuccessHandler)
        );
        //로그인아웃 연결 맵핑
        http.logout(logout->logout
                .logoutSuccessUrl("/login") //로그아웃 성공시 이동할 맵핑명
                .logoutUrl("/logout") //로그아웃 맵핑명
        );
        //csrf 변조방지
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
//보안인증 : 필터링작업=각 권한별로 맵핑에 접근제한 설정(*)
//사용자 로그인폼, 로그아웃처리
//springboot 버전이 바뀔때마다 사용방법이 변경