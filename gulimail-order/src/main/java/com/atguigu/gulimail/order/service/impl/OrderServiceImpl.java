package com.atguigu.gulimail.order.service.impl;

import com.atguigu.common.vo.MemberAddressVo;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.gulimail.order.feign.CartFeign;
import com.atguigu.gulimail.order.feign.MemberFeign;
import com.atguigu.gulimail.order.interceptor.LoginUserInterCeptor;
import com.atguigu.gulimail.order.vo.CartItem;
import com.atguigu.gulimail.order.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.order.dao.OrderDao;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
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
        }, executor);
        //3.优惠信息
        orderVo.setIntegration(memberVO.getIntegration());
        try {
            CompletableFuture.allOf(getAddressFuture,getCartItemsFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return orderVo;
    }

}
