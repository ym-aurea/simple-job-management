package com.simplejob;

public class FailedJob implements Runnable {

    @Override
    public void run() {
        System.out.println("failed job");
        throw new IllegalStateException("it's always failed job");
    }
}
