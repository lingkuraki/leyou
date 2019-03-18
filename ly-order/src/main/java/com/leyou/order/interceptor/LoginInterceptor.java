package com.leyou.order.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    // 定义一个线程存放登录用户
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    private final static Logger LOGGER = LoggerFactory.getLogger(LoginInterceptor.class);

    public LoginInterceptor() {
    }

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        // 解析token
        try {
            // 获取用户
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            // 保存用户
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            // 验证token失败
            LOGGER.error("校验登录状态失败！");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getLoginUser() {
        return tl.get();
    }
}
