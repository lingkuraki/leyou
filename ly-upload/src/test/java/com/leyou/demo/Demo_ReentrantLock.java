package com.leyou.demo;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo_ReentrantLock {

    public static void main(String[] args) {
        Printer p = new Printer();
        Thread t1 = new Thread(() -> {
            try {
                while (true) {
                    p.print1();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                while (true) {
                    p.print2();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread t3 = new Thread(() -> {
            try {
                while (true) {
                    p.print3();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.start();
        t2.start();
        t3.start();
    }
}

class Printer {
    private ReentrantLock lock = new ReentrantLock();
    private Condition c1 = lock.newCondition();
    private Condition c2 = lock.newCondition();
    private Condition c3 = lock.newCondition();
    private int flag = 1;

    public void print1() throws Exception {
        lock.lock();
        if (flag != 1) c1.await(); // 当前线程等待
        System.out.println("面对疾风吧！1");
        flag = 2;
        c2.signal(); // 唤醒c2
        lock.unlock();
    }

    public void print2() throws Exception {
        lock.lock();
        if (flag != 2) c2.await(); // 当前线程等待
        System.out.println("面对疾风吧！2");
        flag = 3;
        c3.signal();
        lock.unlock();
    }

    public void print3() throws Exception {
        lock.lock();
        if (flag != 3) c3.await(); // 当前线程等待
        System.out.println("面对疾风吧！3");
        flag = 1;
        c1.signal();
        lock.unlock();
    }
}