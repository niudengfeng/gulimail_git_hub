package com.atguigu.common;

public enum BizCode {

    LOCK_FAIL(700,"锁定库存失败"),
    FEIGN_FAIL(101,"远程调用服务异常"),
    REQUEST_TOO_FAST(110,"请求过于频繁，限流处理");

    public int code;
    public String msg;

    BizCode(int i, String msg) {
        this.code = i;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
