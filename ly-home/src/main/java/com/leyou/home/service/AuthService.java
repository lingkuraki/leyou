package com.leyou.home.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.home.interceptor.LoginInterceptor;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String authentication() {
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        if (loginUser == null) return null;
        return loginUser.getUsername();
    }
}
