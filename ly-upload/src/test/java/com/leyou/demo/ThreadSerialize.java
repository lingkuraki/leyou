package com.leyou.demo;

public class ThreadSerialize {
 
    public static void main(String[] args){
        ThreadA threadA = new ThreadA();
        ThreadB threadB = new ThreadB();
        ThreadC threadC = new ThreadC();
 
        threadA.setThreadC(threadC);
        threadB.setThreadA(threadA);
        threadC.setThreadB(threadB);
 
        threadA.start();
        threadB.start();
        threadC.start();
 
        while (true){
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
 
  class ThreadA extends Thread{
    private ThreadC threadC;
    @Override
    public void run() {
        while (true){
            synchronized (threadC){
                synchronized (this){
                    System.out.println("I am ThreadA。。。");
                    this.notify();
                }
                try {
                    threadC.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
 
    }
 
    public void setThreadC(ThreadC threadC) {
        this.threadC = threadC;
    }
}
  class ThreadB extends Thread{
    private ThreadA threadA;
    @Override
    public void run() {
        while (true){
            synchronized (threadA){
                synchronized (this){
                    System.out.println("I am ThreadB。。。");
                    this.notify();
                }
                try {
                    threadA.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
 
    }
 
    public void setThreadA(ThreadA threadA) {
        this.threadA = threadA;
    }
}
  class ThreadC extends Thread{
    private ThreadB threadB;
    @Override
    public void run() {
        while (true){
            synchronized (threadB){
                synchronized (this){
                    System.out.println("I am ThreadC。。。");
                    this.notify();
                }
                try {
                    threadB.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
 
    }
 
    public void setThreadB(ThreadB threadB) {
        this.threadB = threadB;
    }
}
