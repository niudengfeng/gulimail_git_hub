package com.atguigu.gulimail.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginVo {

    private String userName;

    private String password;
}
