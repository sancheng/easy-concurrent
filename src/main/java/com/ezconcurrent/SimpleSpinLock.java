package com.ezconcurrent;

/**
 * Created by sancheng on 9/28/2017.
 */
public class SimpleSpinLock {
    private  volatile   boolean inlock = false;


    public void lock()  {
        while (inlock);
        inlock = true;
    }

    public void release() {
        inlock = false;
    }


}
