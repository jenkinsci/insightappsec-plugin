package com.rapid7.insightappsec.intg.jenkins;

public class ThreadHelper {

    static final ThreadHelper INSTANCE = new ThreadHelper();

    private ThreadHelper() {
        // private constructor
    }

    public void sleep(long timeToSleep) throws InterruptedException {
        Thread.sleep(timeToSleep);
    }

}
