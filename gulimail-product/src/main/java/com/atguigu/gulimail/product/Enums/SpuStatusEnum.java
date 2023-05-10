package com.atguigu.gulimail.product.Enums;

public enum SpuStatusEnum {

    NEW(0,"新建"),
    UP(1,"上架"),
    DOWN(2,"下架");

    private Integer code;
    private String msg;

    SpuStatusEnum(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
