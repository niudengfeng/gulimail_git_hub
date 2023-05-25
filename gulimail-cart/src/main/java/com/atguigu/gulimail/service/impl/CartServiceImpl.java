package com.atguigu.gulimail.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.gulimail.feign.ProductFeignService;
import com.atguigu.gulimail.interpt.CartInterpt;
import com.atguigu.gulimail.service.CartService;
import com.atguigu.gulimail.vo.Cart;
import com.atguigu.gulimail.vo.CartItem;
import com.atguigu.gulimail.vo.SkuInfo;
import com.atguigu.common.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItem addCart(Long skuId, int num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String,Object,Object> hashOperations = getHashOperations();
        Object o = hashOperations.get(skuId.toString());
        if (o!=null){
            //说明这个商品之前保存过了，现在只需要累加数量即可
            CartItem item = JSON.parseObject(o.toString(),CartItem.class);
            item.setCount(item.getCount() + num);
            //更新redis
            hashOperations.put(skuId.toString(), JSON.toJSONString(item));
            return item;
        }else {
            //第一次保存当前商品
            CartItem cartItem = new CartItem();
            cartItem.setCount(num);//购物车商品数量
            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                //1.远程调用商品服务根据skuId查询商品SKU信息
                R info = productFeignService.info(skuId);
                if (info.getCode().intValue() == 0) {
                    SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(info.get("skuInfo")), SkuInfo.class);
                    BeanUtils.copyProperties(skuInfo, cartItem);
                }
            }, executor);
            //2.远程调用服务 获取当前商品的组合属性
            CompletableFuture<Void> getSkuSaleListFuture = CompletableFuture.runAsync(() -> {
                List<String> listForCart = productFeignService.getListForCart(skuId);
                cartItem.setSkuAttr(listForCart);
            }, executor);
            //3.等上面两个异步任务全部运行结束后再保存到redis
            CompletableFuture.allOf(getSkuInfoFuture, getSkuSaleListFuture).get();
            hashOperations.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItemBySkuId(String skuId) {
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        Object o = hashOperations.get(skuId);
        if (o==null){
            return null;
        }
        String string = o.toString();
        return JSON.parseObject(string,CartItem.class);
    }

    /**
     * 加载当前用户的购物车数据：
         * 登录用户：需要检查是否有登录前的保存的临时数据，如果有需要合并到登录用户里面去，如果有相同商品需要累计数量，没有就是新增，然后还要清空临时购物车的KEY
         * 临时用户：直接返回临时用户的购物车列表
     * @param userInfo
     * @return
     */
    @Override
    public Cart getCartList(UserInfo userInfo) throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        String keyPrefiex = RedisConstants.CART_USER_KEY_PREFIEX;
        String tempKey = keyPrefiex + userInfo.getUserKeyCookie();//临时数据的KEY
        String userKey = keyPrefiex + userInfo.getUserId();//登录用户的KEY
        List<CartItem> cartList = getCartList(tempKey);//获取临时数据
        if (userInfo.getUserId()==null){
            //未登录，临时用户：直接返回临时用户的购物车列表
            cart.setItems(cartList);
            return cart;
        }else {
            //登录成功的
            //1.先去查询当前用户是否有临时数据
            if (!CollectionUtils.isEmpty(cartList)){
                for (CartItem cartItem : cartList) {
                    //直接循环调用添加购物车的方法，进行把临时的合并到登录用户的
                    addCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //删除临时数据
                redisTemplate.delete(tempKey);
            }
            List<CartItem> userCartList = getCartList(userKey);
            cart.setItems(userCartList);
            return cart;
        }
    }

    /**
     * 改变商品选中状态，同步到redis
     * @param skuId
     * @param check
     * @return
     */
    @Override
    public void changeChecked(Long skuId, boolean check) {
        //1.根据skuId查询到对应商品
        CartItem cartItemBySkuId = getCartItemBySkuId(skuId.toString());
        //2.改变选中状态，重新放入redis
        cartItemBySkuId.setCheck(check);
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        hashOperations.put(skuId.toString(),JSON.toJSONString(cartItemBySkuId));
    }


    /**
     * 改变商品数量，同步到redis
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public void changeCount(Long skuId, int num) {
        //1.根据skuId查询到对应商品
        CartItem cartItemBySkuId = getCartItemBySkuId(skuId.toString());
        //2.改变选中状态，重新放入redis
        cartItemBySkuId.setCount(num);
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        hashOperations.put(skuId.toString(),JSON.toJSONString(cartItemBySkuId));
    }


    /**
     * 删除商品，同步到redis
     * @param skuId
     * @return
     */
    @Override
    public void delItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOperations = getHashOperations();
        hashOperations.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCartsByMemberId() {
        UserInfo userInfo = CartInterpt.threadLocal.get();
        if (userInfo==null){
            //没有登录
            return null;
        }
        String cartKey = RedisConstants.CART_USER_KEY_PREFIEX + userInfo.getUserId();
        List<CartItem> cartList = getCartList(cartKey);
        if (!CollectionUtils.isEmpty(cartList)){
            //需要根据当前skuId拿到最新价格
            /**
             * TODO 下面循环遍历购物项的时候 最好优化下 把所有的skuId遍历出来,一次性请求到所有对应价格信息Map<"skuId",price>
             */
            for (CartItem cartItem : cartList) {
                Long skuId = cartItem.getSkuId();
                R info = productFeignService.info(skuId);
                if (info.getCode().intValue() == 0) {
                    SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(info.get("skuInfo")), SkuInfo.class);
                    BigDecimal price = skuInfo.getPrice();
                    cartItem.setPrice(price);
                }
            }
            return cartList;
        }
        return null;
    }

    /**
     * 绑定购物车的前缀KEY
         * 登录下用用户id
         * 未登录下用userKey
     * @return
     */
    private BoundHashOperations<String,Object,Object> getHashOperations() {
        String key = RedisConstants.CART_USER_KEY_PREFIEX;
        UserInfo userInfo = CartInterpt.threadLocal.get();
        if (userInfo.getUserId()==null){
            //未登录分配userKey
            key = key + userInfo.getUserKeyCookie();
        }else {
            key = key + userInfo.getUserId();
        }

        return redisTemplate.boundHashOps(key);
    }

    /**
     * 根据传递过来的 key 获取数据
     * @param cartKey
     * @return
     */
    public List<CartItem> getCartList(String cartKey){
        ArrayList<CartItem> cartItems = new ArrayList<>();
        BoundHashOperations<String, Object, Object> boud = redisTemplate.boundHashOps(cartKey);
        List<Object> values = boud.values();
        if (!CollectionUtils.isEmpty(values)){
            for (Object value : values) {
                CartItem cartItem = JSON.parseObject(value.toString(), CartItem.class);
                cartItems.add(cartItem);
            }
        }
        return cartItems;
    }
}
