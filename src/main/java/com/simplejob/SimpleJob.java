package com.simplejob;

public class SimpleJob implements Runnable {

    @Override
    public void run() {
        System.out.println("simple Job");
    }
}
