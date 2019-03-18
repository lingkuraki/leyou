package com.leyou.page.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {

    private static final ExecutorService ES = Executors.newFixedThreadPool(10);

    public static void execute(Runnable runnable) {
        ES.submit(runnable);
    }
}
