package com.ezconcurrent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sancheng on 9/28/2017.
 *
 * A Task Tree which parent task waits for all children task completes before it starts its own mission.
 * All child tasks executed concurrently.
 */
public class Task {

    private final List<Runnable> preTasks = new LinkedList<>();
    private Runnable runnable = null;

    public Task join(Task subTask)  {
        preTasks.add(()->subTask.exec());
        return this;
    }

    public Task(Runnable r) {
        this.runnable = r;
    }

    public Task() {

    }

    public  void exec() {
        List<CompletableFuture> futures = new LinkedList<>();


        for (Runnable t : preTasks)  {
            futures.add(CompletableFuture.runAsync(t));
        }

        CompletableFuture currentFuture  = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            currentFuture.get();
            if (runnable!=null) {
                runnable.run();
            }
        } catch (InterruptedException | ExecutionException  e) {
            throw new RuntimeException(e);
        }

    }
}
