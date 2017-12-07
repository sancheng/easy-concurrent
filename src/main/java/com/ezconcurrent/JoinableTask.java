package com.ezconcurrent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by sancheng on 9/28/2017.
 *
 * A JoinableTask Tree which waits for all children task completes before it starts its own mission.
 * All child tasks executed concurrently.
 */
public class JoinableTask {

    private final List<Runnable> preTasks = new LinkedList<>();
    private Runnable runnable = null;

    public JoinableTask join(JoinableTask subJoinableTask)  {
        preTasks.add(()-> subJoinableTask.exec());
        return this;
    }

    public JoinableTask(Runnable r) {
        this.runnable = r;
    }

    public JoinableTask() {

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
