package com.leyou.auth;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;


public class JwtTest {

    private static final String publicKeyPath = "D:/heima/rsa/rsa.pub";

    private final static String privateKeyPath = "D:/heima/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(privateKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "kuraki"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoia3VyYWtpIiwiZXhwIjoxNTQ2Mzk1MTY2fQ.LBMwFFDlrT8UwiVwC6wGmGz3sgRVf0CCndL7vHyD7bT-_u8VCa2bz5uNdSevKcm1gCUjdQ3INfQNtnzK39z3jTL0s0QhseCZ_fwZetNmLQZXdu1qsh10gEJjzxpFb1mQtA3GzqiHKRyv8H7bvbv7cb0tMeJsSS5EEpN0IvpAl3Q";
        // 解析token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id：" + userInfo.getId());
        System.out.println("username：" + userInfo.getUsername());
    }
}
