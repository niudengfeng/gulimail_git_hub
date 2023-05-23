package com.atguigu.gulimail.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@SpringBootTest
class GulimailMemberApplicationTests {

    @Test
    void test() {
        String pre = "123456";
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println("加密前："+pre);
        String encode = bCryptPasswordEncoder.encode(pre);
        System.out.println("加密后："+encode);
        System.out.println("比较:"+bCryptPasswordEncoder.matches(pre, encode));
    }
    @Test
    void test1() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.upgradeEncoding(bCryptPasswordEncoder.encode("123456")));
    }



}
