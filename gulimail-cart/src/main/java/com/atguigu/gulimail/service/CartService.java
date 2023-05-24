package com.atguigu.gulimail.service;

import com.atguigu.gulimail.vo.Cart;
import com.atguigu.gulimail.vo.CartItem;
import com.atguigu.gulimail.vo.UserInfo;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addCart(Long skuId, int num) throws ExecutionException, InterruptedException;

    CartItem getCartItemBySkuId(String string);

    Cart getCartList(UserInfo userInfo) throws ExecutionException, InterruptedException;

    void changeChecked(Long skuId, boolean check);

    void changeCount(Long skuId, int num);

    void delItemBySkuId(Long skuId);
}
