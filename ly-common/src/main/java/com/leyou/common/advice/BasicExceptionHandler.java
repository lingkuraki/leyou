package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class BasicExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        // 记录异常信息
        log.error(e.getMessage(), e);
        // 暂定返回状态码为400，然后从异常中获取友好提示信息
        // return ResponseEntity.status(400).body(e.getMessage());
        // 判断是否是自定义异常
        if (e instanceof LyException) {
            LyException e2 = (LyException) e;
            int code = e2.getStatus() == null ? e2.getStatusCode() : e2.getStatus().value();
            System.out.println(code);
            return ResponseEntity.status(code).body(e.getMessage());
        }
        // 其他情况，返回500
        return ResponseEntity.status(500).body("未知错误！");
    }
}
