/*
package com.leyou.demo;

public class ThreadSerialized {

    public static void main(String[] args) {
        ThreadA ta = new ThreadA();
        ThreadB tb = new ThreadB();
        ThreadC tc = new ThreadC();

        ta.setThreadC(tc);
        tb.setThreadA(ta);
        tc.setThreadB(tb);

        ta.start();
        tb.start();
        tc.start();
    }
}

// 线程A
class ThreadA extends Thread {
    private ThreadC tc;

    @Override
    public void run() {
        while (true) {
            synchronized(tc) {
                synchronized(this) {
                    System.out.println("t1");
                    this.notify();
                }
                try {
                    tc.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setThreadC(ThreadC tc) {
        this.tc = tc;
    }
}

// 线程B
class ThreadB extends Thread {
    private ThreadA ta;

    @Override
    public void run() {
        while (true) {
            synchronized(ta) {
                synchronized(this) {
                    System.out.println("t2");
                    this.notify();
                }
                try {
                    ta.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    void setThreadA(ThreadA ta) {
        this.ta = ta;
    }
}

// 线程C
class ThreadC extends Thread {
    private ThreadB tb;

    @Override
    public void run() {
        while (true) {
            synchronized(tb) {
                synchronized(this) {
                    System.out.println("t3");
                    this.notify();
                }
                try {
                    tb.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setThreadB(ThreadB tb) {
        this.tb = tb;
    }
}*/
