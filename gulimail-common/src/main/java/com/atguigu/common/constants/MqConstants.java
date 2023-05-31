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
    public static final String orderCreateOrderRoutingKey = "order.create.#";
    //释放的routingKey
    public static final String orderReleaseOrderRoutingKey = "order.release.#";
    //订单服务主动关闭订单准备通知库存服务的延迟任务routingKey
    public static final String orderCloseRoutingKeyPrefix = "order.close.";
    //订单服务的交换机和库存服务的死性队列绑定routingKey
    public static final String orderCloseRoutingKey = "order.close.#";


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
