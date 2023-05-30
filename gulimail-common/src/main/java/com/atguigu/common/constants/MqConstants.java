package com.atguigu.common.constants;

public class MqConstants {
    public static final String TESTQUEUENAME = "hello-queue";
    public static final String TESTEXCHANGENAME = "hello-direct-exchange";
    public static final String TESTROUTINGKEY = "hello.world";
    /**
     * 订单服务的延时队列配置
     */
    //交换机
    public static final String orderEventExchange = "order-event-exchange";
    //延时队列
    public static final String orderDelayQueue = "order-delay-queue";
    //死性队列
    public static final String orderReleaseQueue = "order-release-queue";
    //进入延时的创建订单的routingKey
    public static final String orderCreateOrderRoutingKey = "order-create-order";
    //释放的routingKey
    public static final String orderReleaseOrderRoutingKey = "order-release-order";


    /**
     * 仓库服务的延时队列配置
     */
    //交换机
    public static final String stockEventExchange = "stock-event-exchange";
    //延时队列
    public static final String stockDelayQueue = "stock-delay-queue";
    //死性队列
    public static final String stockReleaseQueue = "stock-release-queue";
    //锁定库存的延时routingKey
    public static final String stockLockRoutingKey = "stock.lock.#";
    //释放消息的解锁routingKey
    public static final String stockReleaseRoutingKey = "stock.release.#";

}
