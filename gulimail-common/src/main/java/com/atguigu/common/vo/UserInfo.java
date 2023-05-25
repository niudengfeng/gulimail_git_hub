package com.atguigu.common.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfo {

    private Long userId;

    private String userName;

    private String userKeyCookie;

    private String phone;

    /**
     * 是否临时用户
     */
    private boolean isTemFlag = false;

}
