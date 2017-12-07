package com.ezconcurrent.workflow;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Created by sancheng on 11/24/2017.
 *
 * a easy to understand higher level predicates for concurrnet executions.
 *
 * for example:
 *
 * ConcurrentFlow.split(task1,task2,task3).minmumSucceed(2).logFailure().converge().split(task4,task5).anySucceed().converge();
 * ConcurrentFlow.nextTask(task1).logFailure();
 *
 * if we need pass upstream execution results:
 *
 * Predicates.node(task1).converge().pipe().node(task2).converge()
 *
 *
 */
public interface ConcurrentFlow<T> {

    public ConcurrentFlow anySucceed();

    public ConcurrentFlow minmumSucceed(int number);

    public ConcurrentFlow finishCallback(Callable call);

    public ConcurrentFlow failureCallback(Callable call);

    public ConcurrentFlow pipe();

    public ConcurrentFlow next(Callable call);

    public ConcurrentFlow next(Consumer consumer);

    public ConcurrentFlow split(Callable... calls);

    public T start();



}
