package com.gyr.minio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyr.minio.filter.WebSecurityCorsFilter;
import com.gyr.minio.service.CustomAuthenticationProvider;
import com.gyr.minio.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(customAuthenticationProvider)
                .userDetailsService(customUserDetailsService).passwordEncoder(NoOpPasswordEncoder.getInstance());

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    httpServletResponse.setContentType("application/json;charset=utf-8");
                    PrintWriter out = httpServletResponse.getWriter();
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 200);
                    map.put("msg", "登录成功");
                    map.put("data", authentication.getPrincipal());
                    ObjectMapper objectMapper = new ObjectMapper();
                    out.write(objectMapper.writeValueAsString(map));
                    out.flush();
                    out.close();
                })
                .failureHandler((httpServletRequest, httpServletResponse, e) -> {
                    httpServletResponse.setContentType("application/json;charset=utf-8");
                    PrintWriter out = httpServletResponse.getWriter();
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 401);
                    if (e instanceof LockedException) {
                        map.put("msg", "账户被锁定");
                    } else if (e instanceof BadCredentialsException) {
                        map.put("msg", "用户名或密码错误");
                    } else if (e instanceof DisabledException) {
                        map.put("msg", "账户被禁用");
                    } else if (e instanceof AccountExpiredException) {
                        map.put("msg", "账户已过期");
                    } else if (e instanceof CredentialsExpiredException) {
                        map.put("msg", "密码已过期");
                    } else map.put("msg", "登录失败");
                    ObjectMapper objectMapper = new ObjectMapper();
                    out.write(objectMapper.writeValueAsString(map));
                    out.flush();
                    out.close();
                })
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((httpServletRequest, httpServletResponse, e) -> {
                    httpServletResponse.setContentType("application/json;charset=utf-8");
                    PrintWriter out = httpServletResponse.getWriter();
                    Map<String, Object> map = new HashMap<>();
                    if (e instanceof InsufficientAuthenticationException) {
                        map.put("status", 401);
                        map.put("msg", "未登录");
                    }
                    out.write(new ObjectMapper().writeValueAsString(map));
                    out.flush();
                    out.close();
                })
                .and()
                .logout()
                .logoutSuccessHandler(((httpServletRequest, httpServletResponse, authentication) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", 200);
                    map.put("msg", "登出成功");
                    map.put("data", authentication);
                    httpServletResponse.setContentType("application/json;charset=utf-8");
                    PrintWriter out = httpServletResponse.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(map));
                    out.flush();
                    out.close();
                }))
                .and()
                .sessionManagement()
                .invalidSessionUrl("/session/invalid")
                .and()
                .csrf().disable()
                .addFilterBefore(new WebSecurityCorsFilter(), ChannelProcessingFilter.class);
    }
}
