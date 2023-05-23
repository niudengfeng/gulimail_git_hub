package com.atguigu.gulimail.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class LoginVo {

    @NotEmpty(message = "用户名必须填写")
    @Length(min = 6,max = 18,message = "用户名必须是6-18字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码必须是6-18字符")
    private String password;
}
