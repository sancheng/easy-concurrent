package com.ezconcurrent.workflow;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by sancheng on 11/30/2017.
 */
public class SimpleConcurrentFlow<T> implements  ConcurrentFlow<T>{

    //private static class State  {

    static class Cmd  {
        Callable[] calls;
        String completePolicy = "all";

        public Cmd calls(Callable[] calls) {
            this.calls = calls;
            return this;
        }

        public Cmd completePolicy(String policy) {
            this.completePolicy = policy;
            return this;
        }

        public Object exec() throws ExecutionException, InterruptedException  {


            if ("any".equals(completePolicy))  {

                List<CompletableFuture> futures = new LinkedList<>();
                for (Callable call : calls)  {
                    futures.add(CompletableFuture.supplyAsync(() -> {

                        try {
                            return call.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }));
                }
                CompletableFuture currentFuture  = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
                return currentFuture.get();
            } else if ("all".equals(completePolicy))  {
                List<CompletableFuture> futures = new LinkedList<>();
                ArrayBlockingQueue q = new ArrayBlockingQueue(calls.length);
                for (Callable call : calls)  {
                    futures.add(CompletableFuture.supplyAsync(() -> {

                        try {
                            Object res  =call.call();
                            if(res !=null)
                             q.offer(res);
                            return res;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                // currentFuture.get();
                return Arrays.asList(q.toArray());
            } else if (completePolicy.startsWith("num."))  {
                int minSucceedNum = Integer.valueOf(completePolicy.substring(4));
                List<CompletableFuture> futures = new LinkedList<>();
                Phaser phaser = new Phaser(minSucceedNum+1);
                ArrayBlockingQueue q = new ArrayBlockingQueue(minSucceedNum);
                for (Callable call : calls)  {
                    futures.add(CompletableFuture.supplyAsync(() -> {

                        try {
                            Object res = call.call();
                             if (res != null) q.offer(res);
                             phaser.arriveAndDeregister();
                             return res;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }));

                }
                phaser.arriveAndAwaitAdvance();
                return Arrays.asList(q.toArray());
            }

            return null;
        }
    }

        CompletableFuture future = new CompletableFuture();

        Queue<Callable> tasks = new LinkedList<>();

        int concurrentTaskNumber = 0;

        Queue<Cmd> cmd = new LinkedList<>();

        Stack resultStack = new Stack();


   public T start() {
       Object obj = null;
       while (cmd.size() > 0) {
           Cmd oneCmd = cmd.poll();
           try {
               obj = oneCmd.exec();
               resultStack.push(obj);
           } catch (ExecutionException|InterruptedException e) {
              throw new RuntimeException(e);
           }
       }
       return (T) obj;
   }


    //}
    @Override
    public ConcurrentFlow anySucceed() {
        if (cmd.size() ==0)  {return this;}
        Cmd lastCmd = cmd.poll();
        cmd.offer( lastCmd.completePolicy("any"));
        return this;
    }

    @Override
    public ConcurrentFlow minmumSucceed(int minNum) {
        if (cmd.size() ==0)  {return this;}
        Cmd lastCmd = cmd.poll();
        cmd.offer( lastCmd.completePolicy("num." + String.valueOf(minNum)));
       return this;
    }




    @Override
    public ConcurrentFlow finishCallback(Callable call) {
        cmd.offer(new Cmd().calls(new Callable[]{call}));
        return this;

    }

    @Override
    public ConcurrentFlow failureCallback(Callable call) {
        return null;
    }

    @Override
    public ConcurrentFlow pipe() {
        return this;
    }

    @Override
    public ConcurrentFlow next(Callable call) {
       cmd.offer(new Cmd().calls(new Callable[]{call}));
        return this;
    }

    @Override
    public ConcurrentFlow next(final Consumer consumer) {
        Object input = null;

       final Object feedInput = input;
       Callable call = new Callable() {
           @Override
           public Object call() throws Exception {
               if (resultStack.size() > 0)  {
                   Object input = resultStack.peek();
                   consumer.accept(input);
               }

                return null;
           }
       };
        cmd.offer(new Cmd().calls(new Callable[]{call}));
        return this;
    }

    @Override
    public ConcurrentFlow split(Callable[] calls) {
        if (calls == null || calls.length == 0)  { return this;}
        cmd.offer(new Cmd().calls(calls));
        return this;
    }

    public static void main(String[] args) {
        ConcurrentFlow cf = new SimpleConcurrentFlow();
        Object obj = cf.split(new Callable() {
            @Override
            public Object call() throws Exception {
                return "1";
            }
        }, new Callable() {
            @Override
            public Object call() throws Exception {
                return "2";
            }
        },new Callable() {
            @Override
            public Object call() throws Exception {
                return "3";
            }
        },new Callable() {
            @Override
            public Object call() throws Exception {
                return "4";
            }
        }).minmumSucceed(2).pipe().next((x)-> System.out.println(x)).start();

        System.out.println(obj);

    }
}
