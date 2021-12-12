package com.gyr.minio.service;

import com.gyr.minio.redis.PasswordRetryTimesRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    PasswordRetryTimesRedis redisUtil;

    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        super.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        try {
            int retryTimes = redisUtil.getRetryTimes(userName);
            if (retryTimes >= 3) throw new LockedException("24小时内输入次数超过3次，冻结账户24小时!");
            Authentication authenticate = super.authenticate(authentication);
            redisUtil.removeKey(authentication.getName()); // 清除对用户的监控
            return authenticate;
        } catch (BadCredentialsException badCredentialsException) {
            redisUtil.addRetryTimes(userName);
            throw badCredentialsException;
        }
    }
}
