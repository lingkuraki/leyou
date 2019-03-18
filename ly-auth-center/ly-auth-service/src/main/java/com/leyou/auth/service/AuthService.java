package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private UserClient userClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public String authentication(String username, String password) {
        try {
            // 查询用户
            User user = this.userClient.queryUser(username, password);
            // 判断是否为空
            if (user == null) return null;
            // 创建用户信息
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername());
            // 生成token
            String token = JwtUtils.generateToken(userInfo, jwtProp.getPrivateKey(), jwtProp.getExpire());
            return token;
        } catch (Exception e) {
            LOGGER.error("登录失败：用户名：{}", username, e);
            return null;
        }
    }
}
