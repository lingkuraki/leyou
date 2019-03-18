package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    // 密钥
    private String secret;
    // 私钥路径
    private String pubKeyPath;
    // 私钥路径
    private String priKeyPath;
    // token过期时间
    private int expire;
    // 公钥
    private PublicKey publicKey;
    // 私钥
    private PrivateKey privateKey;
    // cookie的过期时间
    private String cookieMaxAge;
    // cookie名称
    private String cookieName;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProperties.class);

    @PostConstruct
    public void init() {
        try {
            File pubKey = new File(pubKeyPath);
            File priKey = new File(priKeyPath);
            if (!pubKey.exists() || !priKey.exists()) {
                // 生成公钥和私钥
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            LOGGER.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException();
        }
    }
}
