package com.simplejob;

import java.util.concurrent.TimeUnit;

public class LongRunningJob implements Runnable {

    @Override
    public void run() {
        while (true) {
            System.out.println("long running Job");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}