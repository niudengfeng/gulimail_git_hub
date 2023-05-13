package com.atguigu.gulimail.product.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    //定义一个固定十个线程的线程池
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //无返回
//        CompletableFuture.runAsync(()->{
//            System.out.println("runAsync开始-结束");
//        },executorService);

//        有返回 调用whenComplete但是不可以处理返回值 不过可以有返回值
//        CompletableFuture.supplyAsync(()->{
//            System.out.println("supplyAsync");
//            return 1;
//        },executorService).whenComplete((t,u)->{
//            System.out.println("res:"+t);
//            System.out.println("异常："+u);
//        });
//
        //有返回调用handle可以处理结果值
//        CompletableFuture<Integer> supplyAsync = CompletableFuture.supplyAsync(() -> {
//            System.out.println("supplyAsync");
//            return 1;
//        }, executorService).handle((t, u) -> {
//            if (t != null) {
//                return t * 2;
//            }
//            return 0;
//        });
//        System.out.println("supplyAsync结果:"+supplyAsync.get());


        /**
         * todo 1.线程串行化
         *
         * thenRunAsync 等第一个执行完后，在开启一个新线程里面继续执行第二个，无返回
         * thenRun 等第一个执行完后，在同一个线程里面继续执行第二个，无返回
         */
        /*CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync");
            return 1;
        }, executorService).thenRunAsync(
                ()->{
                    System.out.println("第二个线程开始了...");
                },executorService
        );*/


        /**
         * todo 2.线程串行化
         * thenAcceptAsync 可以接收上个任务的返回值，在开启一个新线程里面继续执行下一个任务,无返回
         */
       /* CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync");
            return 1;
        }, executorService).thenAcceptAsync((t)->{
            System.out.println("接收到上一个任务的返回值："+t);
        },executorService);*/

        /**
         * todo 3.线程串行化
         * thenApplyAsync 可以接收上个任务的返回值，在开启一个新线程里面继续执行下一个任务,并且有返回
         */
  /*      CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync");
            return 1;
        }, executorService).thenApplyAsync((t) -> {
            System.out.println("接收到上个任务的返回值t："+t);
            return t+"_";
        }, executorService);
        System.out.println("thenApplyAsync执行完结果:"+supplyAsync.get());
*/


        /**
         * todo 4.组合任务，两个必须全部完成，才会触发第三个任务
         */
        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1结束");
            return "hello,";
        }, executorService);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始");
            System.out.println("任务2结束");
            return "world!";
        }, executorService);

        /**
         * todo 4.1 runAfterBothAsync 不接收前面任务的参数 也不能返回 ，只能感知到这两个任务全部结束，我再处理别的业务
         */
       /* future01.runAfterBothAsync(future02,()->{
            System.out.println("任务3开始...");
        },executorService);
*/
        /**
         * todo 4.2 thenAcceptBothAsync 接收前面任务的参数 但是不能返回
         */
       /* future01.thenAcceptBothAsync(future02,(f1,f2)->{
            System.out.println("任务3开始=》f1:"+f1+",f2:"+f2);
        },executorService);*/

        /**
         * todo 4.3 thenCombineAsync 接收前面任务的参数 能返回
         */
        /*CompletableFuture<String> stringCompletableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
            System.out.println("任务3开始=》f1:" + f1 + ",f2:" + f2);
            return f1 + f2;
        }, executorService);
        System.out.println("任务3处理完返回："+stringCompletableFuture.get());*/



        /**
         * todo 5.组合任务，两个只要有一个完成，就会触发第三个任务
         */
        /**
         * todo 5.1 runAfterEitherAsync，不接收参数，也无返回值
         */
        /*future01.runAfterEitherAsync(future02,()->{
            System.out.println("第三个任务开始");
        },executorService);*/

        /**
         * todo 5.2 acceptEitherAsync，接收参数，无返回值
         */
        /*future01.acceptEitherAsync(future02,(t)->{
            System.out.println("第三个任务开始,拿到上个任务的返回值："+t);
        },executorService);*/

        /**
         * todo 5.3 applyToEitherAsync，接收参数，有返回值
         */
        /*System.out.println("applyToEitherAsync有返回:"+future01.applyToEitherAsync(future02, (t) -> {
            System.out.println("第三个任务开始,拿到上个任务的返回值：" + t);
            return t + "_";
        }, executorService).get());*/


        /**
         * todo 6.多任务调用
         */

        /**
         * todo 6.1 allOf() 组合多个任务，必须等待所有任务完成后再结束，无返回，需要各个任务的返回值，需要自己去调用各自任务的get方法
         */
        /*CompletableFuture<Void> allOf = CompletableFuture.allOf(future01, future02);
        allOf.get();
        System.out.println("allOf运行后调用allOf.get()后查看结果："+future01.get()+"=>"+future02.get());*/


        /**
         * todo 6.2 anyOf() 组合多个任务，只要有一个任务完成后就立马返回
         */
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future01, future02);
        Object o = anyOf.get();
        System.out.println("anyOf运行后调用anyOf.get()后查看结果："+o);
    }

}
