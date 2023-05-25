package com.atguigu.gulimail.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    private Integer countNum;//商品数量
    private Integer countType;//商品类型数量
    private BigDecimal totalAmount;//商品总价
    private BigDecimal reduce = BigDecimal.ZERO;//诚免价格

    public Integer getCountNum() {
        countNum = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem item : items) {
                countNum = countNum + item.getCount();
            }
        }
        return countNum;
    }

    public Integer getCountType() {
        if (CollectionUtils.isEmpty(items)){
            return 0;
        }
        return items.size();
    }

    public BigDecimal getTotalAmount() {
        //1.计算所有商品总价
        totalAmount = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem item : items) {
                if (item.getCheck()){
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        //2.减去优惠减免的金额
        totalAmount = totalAmount.subtract(this.reduce);
        return totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal bigDecimal){
        this.reduce = bigDecimal;
    }

    public void setItems(List<CartItem> cartItems){
        this.items = cartItems;
    }

    public List<CartItem> getItems(){
        return this.items;
    }
}
