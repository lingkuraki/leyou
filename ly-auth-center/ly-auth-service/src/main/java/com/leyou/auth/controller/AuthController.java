package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @PostMapping("login")
    public ResponseEntity<Void> authentication(
            @RequestParam("username") String username, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response) {
        // 登录校验
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 将token写入cookie，并指定httpOnly为true，防止通过JS获取和修改
        CookieUtils.newBuilder(response).request(request).httpOnly().build(prop.getCookieName(), token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 如果成功，重新生成token
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            // 将新生成的token写入cookie
            CookieUtils.newBuilder(response).request(request).httpOnly().build(prop.getCookieName(), newToken);
            // 成功后直接返回
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // 抛出异常，证明token无效，直接返回401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
