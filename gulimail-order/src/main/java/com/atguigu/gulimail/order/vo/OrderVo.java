package com.atguigu.gulimail.order.vo;

import com.atguigu.common.vo.MemberAddressVo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ToString
public class OrderVo {

    //收货地址集合
    @Getter @Setter
    List<MemberAddressVo> address;

    //所有选中的购物项
    @Getter @Setter
    List<CartItem> cartItems;

    //优惠信息
    @Getter @Setter
    private Integer integration;

    //防重令牌,防止重复提交
    @Getter @Setter
    private String orderToken;

    //订单总额
    BigDecimal totalAmount;

    //应付金额 暂时没做优惠劵 所以和订单总额是一样的
    BigDecimal payAmount;

    int totalCount;//所有购物项的商品总件数

    public int getTotalCount(){
        int i=0;
        if (!CollectionUtils.isEmpty(this.cartItems)){
            for (CartItem cartItem : cartItems) {
                i = i + cartItem.getCount();
            }
        }
        return i;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(this.cartItems)){
            for (CartItem cartItem : cartItems) {
                BigDecimal multiply = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayAmount() {
        return this.getTotalAmount();
    }

}
