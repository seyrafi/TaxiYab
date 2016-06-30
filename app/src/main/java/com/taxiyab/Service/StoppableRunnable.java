package com.taxiyab.Service;

/**
 * Created by MehrdadS on 6/22/2016.
 */
public abstract class StoppableRunnable implements Runnable {

    private volatile boolean mIsStopped = false;

    public boolean isStopped() {
        return mIsStopped;
    }

    public void Stop() {
        mIsStopped = true;
    }
}