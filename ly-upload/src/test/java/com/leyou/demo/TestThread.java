package com.leyou.demo;

public class TestThread {
    static Thread t1 = new Thread(() -> System.out.println("t1"));

    static Thread t2 = new Thread(() -> {
        try {
            t1.join();
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("t2");
    });

    static Thread t3 = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                t2.join();
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.print("t3");
        }
    });

    public static void main(String[] args) {
        t1.start();
        t2.start();
        t3.start();
    }
}
