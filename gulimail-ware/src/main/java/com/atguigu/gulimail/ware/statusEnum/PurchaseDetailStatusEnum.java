package com.atguigu.gulimail.ware.statusEnum;

public enum PurchaseDetailStatusEnum {

    CREATE(0,"新建"),
    ASSIGNED(1,"已分配"),
    SHOPPING(2,"正在采购"),
    FINISH(3,"已完成"),
    HASERROR(4,"采购失败");

    private Integer code;
    private String msg;

    PurchaseDetailStatusEnum(Integer code, String msg){
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
