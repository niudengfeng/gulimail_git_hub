package com.atguigu.common.exception;

import lombok.Data;

public class NoStockException extends RuntimeException {

    public NoStockException(Long skuId){
        super("商品ID:"+skuId.toString() + " 没有库存了");
    }

}
