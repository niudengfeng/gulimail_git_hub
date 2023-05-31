package com.atguigu.gulimail.order.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.constants.MqConstants;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.*;
import com.atguigu.gulimail.order.entity.OrderItemEntity;
import com.atguigu.gulimail.order.enume.OrderStatusEnum;
import com.atguigu.gulimail.order.feign.ProductFeign;
import com.atguigu.gulimail.order.service.OrderItemService;
import com.atguigu.gulimail.order.to.OrderResponseTo;
import com.atguigu.gulimail.order.vo.SpuInfoVo;
import com.atguigu.gulimail.order.vo.SubmitOrderResponseVo;
import com.atguigu.gulimail.order.feign.CartFeign;
import com.atguigu.gulimail.order.feign.MemberFeign;
import com.atguigu.gulimail.order.feign.WareFeign;
import com.atguigu.gulimail.order.interceptor.LoginUserInterCeptor;
import com.atguigu.gulimail.order.vo.CartItem;
import com.atguigu.gulimail.order.vo.OrderVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.order.dao.OrderDao;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private CartFeign cartFeign;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeign wareFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 远程调用 不可避免的会出现分布式事务问题，引入Seata
     * 但是这里是下单接口，高并发下不适用seata，因为AT模式下的原理是利用本地锁+全局事务锁来结合完成的。引入MQ延时队列完成
     * @param submitOrderVo
     * @return
     */
    @Override
//    @GlobalTransactional//seata全局事务
    @Transactional
    public SubmitOrderResponseVo createOrder(SubmitOrderVo submitOrderVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberVO memberVO = LoginUserInterCeptor.threadLocal.get();
        Long userId = memberVO.getId();
        //1.验证令牌：这里需要保证检查和删除是一个原子操作，准备LUA脚本
        String key = RedisConstants.ORDER_TOKEN_USER_PREFIX + userId;
        String scipt = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = submitOrderVo.getOrderToken();
        //脚本执行后返回LONG类型 0代表失败  1代表成功
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(scipt,Long.class), Arrays.asList(key), orderToken);
        if (execute == 0L){
            //令牌校验失败
            responseVo.setCode(100);
            return responseVo;
        }else {
            //成功
            //2.创建订单
            OrderResponseTo orderResponseTo = create(submitOrderVo);
            //我们自己计算的应付金额
            OrderEntity order = orderResponseTo.getOrder();
            BigDecimal payAmount = order.getPayAmount();
            //页面传过来的应付金额
            BigDecimal payAmount1 = submitOrderVo.getPayAmount();
            //3.验价：需要验证：这里只要相差金额小于0.01都算可以
            if (Math.abs(payAmount.subtract(payAmount1).doubleValue())<0.01){
                //4.保存订单
                this.save(order);
                //5.保存订单项
                orderItemService.saveBatch(orderResponseTo.getOrderItemEntityList());
                //6.锁定库存
                OrderLockVO orderLockVO = new OrderLockVO();
                orderLockVO.setOrderSn(order.getOrderSn());
                List<OrderItem> orderItems = orderResponseTo.getOrderItemEntityList().stream().map(m -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setCount(m.getSkuQuantity());
                    orderItem.setSkuId(m.getSkuId());
                    orderItem.setSkuName(m.getSkuName());
                    return orderItem;
                }).collect(Collectors.toList());
                orderLockVO.setOrderItems(orderItems);
                //远程锁定库存
                R r = wareFeign.lockStock(orderLockVO);
                if (r.getCode()==0){
                    //锁定成功
                    responseVo.setOrder(order);
                    responseVo.setCode(0);
                    //模拟错误，看能否回滚库存服务里面的事务int a = 10/0;
                    //到这里是真的订单创建全部成功了，需要发个消息给mq 延时半小时处理，如果未付款，直接关闭订单
                    rabbitTemplate.convertAndSend(MqConstants.orderEventExchange,
                            "order.create."+order.getOrderSn(),order,
                            new CorrelationData(order.getOrderSn()));
                    return responseVo;
                }else {
                    //锁定库存失败
                    throw new NoStockException(order.getId());
                }
            }else {
                //验价失败
                responseVo.setCode(300);
                return responseVo;
            }
        }
        /*if (!StringUtils.isEmpty(orderToken) && orderToken.equals(redisToken)){
            //验证成功，删除令牌
            redisTemplate.delete(key);
        }else {
            responseVo.setCode(100);
        }*/
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        QueryWrapper<OrderEntity> orderEntityQueryWrapper = new QueryWrapper<>();
        orderEntityQueryWrapper.eq("order_sn",orderSn);
        List<OrderEntity> orderEntities = this.getBaseMapper().selectList(orderEntityQueryWrapper);
        if (CollectionUtils.isEmpty(orderEntities)){
            return null;
        }else {
            return orderEntities.get(0);
        }
    }

    /**
     *              * TODO 问题1:我们feign远程调用会给我们重新封装个request请求,我们会丢失原请求的header,自然也就丢失了session cookie数据了
     *              * TODO 解决:封装的时候会循环判断所有的RequestInterceptor,所以我们需要配置个RequestInterceptor把当前请求的头信息带过去,人家遍历到这个RequestInterceptor,封装新请求就会带上了
     *              * TODO 问题2:开启异步变成有个问题就是会开多个线程去执行feign的调用,这样threadLocal的数据就不能共享了,所以又会丢请求头信息了,出现上面的问题
     *              * TODO 解决:每次远程调用之前都先执行:RequestContextHolder.setRequestAttributes(requestAttributes);把当前请求的reqeust信息取出来,然后大家调用之前先同步进去
     * @return
     */
    @Override
    public OrderVo toTrade() {
        MemberVO memberVO = LoginUserInterCeptor.threadLocal.get();
        OrderVo orderVo = new OrderVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.根据当前会员ID获取收货地址集合
            Long memberId = memberVO.getId();

            RequestContextHolder.setRequestAttributes(requestAttributes);

            List<MemberAddressVo> listByMemberId = memberFeign.getListByMemberId(memberId);
            /*if (!CollectionUtils.isEmpty(listByMemberId)){
                listByMemberId = listByMemberId.stream().map(m->{
                    int i = RandomUtil.randomInt(1, 10);
                    m.setMoneyYun(new BigDecimal(i));
                    return m;
                }).collect(Collectors.toList());
            }*/
            orderVo.setAddress(listByMemberId);
        }, executor);

        CompletableFuture<Void> getCartItemsFuture = CompletableFuture.runAsync(() -> {
            //2.所有选中的购物项
            /**
             * TODO 问题1:我们feign远程调用会给我们重新封装个request请求,我们会丢失原请求的header,自然也就丢失了session cookie数据了
             * TODO 解决:封装的时候会循环判断所有的RequestInterceptor,所以我们需要配置个RequestInterceptor把当前请求的头信息带过去,人家遍历到这个RequestInterceptor,封装新请求就会带上了
             * TODO 问题2:开启异步变成有个问题就是会开多个线程去执行feign的调用,这样threadLocal的数据就不能共享了,所以又会丢请求头信息了,出现上面的问题
             * TODO 解决:每次远程调用之前都先执行:RequestContextHolder.setRequestAttributes(requestAttributes);把当前请求的reqeust信息取出来,然后大家调用之前先同步进去
             */

            RequestContextHolder.setRequestAttributes(requestAttributes);

            List<CartItem> cartItems = cartFeign.getCartsByMemberId();
            orderVo.setCartItems(cartItems);
        }, executor).thenRunAsync(()->{
            List<CartItem> cartItems = orderVo.getCartItems();
            if (!CollectionUtils.isEmpty(cartItems)){
                List<Long> skuIds = cartItems.stream().map(CartItem::getSkuId).collect(Collectors.toList());
                RequestContextHolder.setRequestAttributes(requestAttributes);
                //从库存ware系统中根据skuIds查询对应所有的库存信息
                R r = wareFeign.hasStock(skuIds);
                if (r.getCode()==0){
                    Map<String,Boolean> data = JSON.parseObject(JSON.toJSONString(r.get("data")), Map.class);
                    for (CartItem item : cartItems) {
                        item.setHasStock(data.get(item.getSkuId().toString()));
                    }
                    orderVo.setCartItems(cartItems);
                }
            }
        },executor);
        //3.优惠信息
        orderVo.setIntegration(memberVO.getIntegration());
        try {
            CompletableFuture.allOf(getAddressFuture,getCartItemsFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        //4.设置订单防重令牌

        String token = UUID.randomUUID().toString();
        orderVo.setOrderToken(token);//给页面一份，等会提交订单去校验这个TOKEN
        String orderTokenUserPrefix = RedisConstants.ORDER_TOKEN_USER_PREFIX;
        String key = orderTokenUserPrefix + memberVO.getId();
        redisTemplate.opsForValue().set(key,token,30, TimeUnit.MINUTES);//默认存储30分钟，然后令牌失效
        return orderVo;
    }

    /**
     *              * 	 * 订单总额
     *              *           	private BigDecimal totalAmount;
     *              * 	 * 应付总额
     *              *
     *              * 	privateBigDecimal payAmount;
     * @param submitOrderVo
     * @return
     */

    private OrderResponseTo create(SubmitOrderVo submitOrderVo){
        MemberVO memberVO = LoginUserInterCeptor.threadLocal.get();
        Long memberId = memberVO.getId();
        OrderResponseTo orderResponseTo = new OrderResponseTo();
        //1.构建订单
        OrderEntity orderEntity = builderOrderEntity(submitOrderVo, memberVO, memberId);
        //2.构建订单项
        String orderSn = orderEntity.getOrderSn();//订单唯一号
        List<OrderItemEntity> orderItems = builderOrderItems(orderSn);
        //3.计算价格
        computePrice(orderItems,orderEntity);
        orderResponseTo.setOrderItemEntityList(orderItems);
        orderResponseTo.setOrder(orderEntity);
        return orderResponseTo;
    }

    private void computePrice(List<OrderItemEntity> orderItems, OrderEntity orderEntity) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;
        BigDecimal couponAmount = BigDecimal.ZERO;
        BigDecimal integrationAmount = BigDecimal.ZERO;

        Integer giftGrowth = 0;
        Integer giftIntegration = 0;
        //订单总额：把每一个订单项的实际金额(去除各种减免优惠)累计
        for (OrderItemEntity orderItem : orderItems) {
            BigDecimal realAmount = orderItem.getRealAmount();
            total = total.add(realAmount);
            promotionAmount = promotionAmount.add(orderItem.getPromotionAmount());
            couponAmount = couponAmount.add(orderItem.getCouponAmount());
            integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount());

            giftGrowth = giftGrowth + orderItem.getGiftGrowth();
            giftIntegration = giftIntegration + orderItem.getGiftIntegration();
        }
        orderEntity.setTotalAmount(total);
        //实际所需支付金额=订单总额+运费
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegration);
    }

    /**
     * 构造订单项
     * @return
     */
    private List<OrderItemEntity> builderOrderItems(String orderSn) {
        //获取当前用户的购物车项
        List<CartItem> cartsByMemberId = cartFeign.getCartsByMemberId();
        //遍历构造订单项数据
        if (!CollectionUtils.isEmpty(cartsByMemberId)){
            List<OrderItemEntity> orderItems = cartsByMemberId.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = builderOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItems;
        }
        return null;
    }

    /**
     * 构造订单基础数据
     * @param submitOrderVo
     * @param memberVO
     * @param memberId
     * @return
     */
    private OrderEntity builderOrderEntity(SubmitOrderVo submitOrderVo, MemberVO memberVO, Long memberId) {
        OrderEntity orderEntity = new OrderEntity();
        String orderSn = IdWorker.getTimeId();
        //2.封装基本信息
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberId);
        orderEntity.setCreateTime(new Date());
        orderEntity.setMemberUsername(memberVO.getUsername());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setModifyTime(new Date());
        //3.获取收货人相关信息
        List<MemberAddressVo> listByMemberId = memberFeign.getListByMemberId(memberId);
        if (! CollectionUtils.isEmpty(listByMemberId)){
            List<MemberAddressVo> collect = listByMemberId.stream().filter(f -> f.getId().equals(submitOrderVo.getAddrId())).collect(Collectors.toList());
            //得到当前收货地址信息
            MemberAddressVo memberAddressVo = collect.get(0);
            //运费
            orderEntity.setFreightAmount(memberAddressVo.getMoneyYun());
            //收货人姓名
            orderEntity.setReceiverName(memberAddressVo.getName());
            //收货人电话
            orderEntity.setReceiverPhone(memberAddressVo.getPhone());
            //收货人邮编
            orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
            //省份/直辖市
            orderEntity.setReceiverProvince(memberAddressVo.getProvince());
            //城市
            orderEntity.setReceiverCity(memberAddressVo.getCity());
            //区
            orderEntity.setReceiverRegion(memberAddressVo.getRegion());
            //详细地址
            orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
            //订单备注
            orderEntity.setNote(submitOrderVo.getBak());
        }
        return orderEntity;
    }

    /**
     * 构造订单单独项
     * @param cartItem
     * @return
     */
    private OrderItemEntity builderOrderItem(CartItem cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1.订单信息 订单编号 已经在上面封装好了订单号
        //2.SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeign.getInfoBySkuId(skuId);
        if (r.getCode()==0){
            Object o = r.get("spuInfo");
            if (o!=null){
                SpuInfoVo spuInfoVo = JSON.parseObject(JSON.toJSONString(o), SpuInfoVo.class);
                orderItemEntity.setSpuId(spuInfoVo.getId());
                orderItemEntity.setSpuName(spuInfoVo.getSpuName());
                orderItemEntity.setSpuBrandId(spuInfoVo.getBrandId());
                orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
            }
        }
        //3.sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getSkuTitle());
        orderItemEntity.setSkuPic(cartItem.getSkuDefaultImg());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        //4.优惠券信息【不做】
        //5.积分信息
        BigDecimal decimal = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()));
        orderItemEntity.setGiftGrowth(decimal.intValue());
        orderItemEntity.setGiftIntegration(decimal.intValue());
        //各种优惠金额，可以远程调用查询。这里写死
        BigDecimal promotionAmount =  BigDecimal.ZERO;
        BigDecimal couponAmount =  BigDecimal.ZERO;
        BigDecimal integrationAmount =  BigDecimal.ZERO;
        orderItemEntity.setPromotionAmount(promotionAmount);
        orderItemEntity.setCouponAmount(couponAmount);
        orderItemEntity.setIntegrationAmount(integrationAmount);
        //6.实际支付金额 减去优惠金额
        BigDecimal multiply = new BigDecimal(cartItem.getCount().toString()).multiply(cartItem.getPrice());
        multiply = multiply.subtract(promotionAmount)
                .subtract(couponAmount)
                .subtract(integrationAmount);
        orderItemEntity.setRealAmount(multiply);
        return orderItemEntity;
    }

}
