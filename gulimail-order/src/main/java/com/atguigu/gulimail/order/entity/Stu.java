package com.atguigu.gulimail.order.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ToString
public class Stu    {
    private int age;
    private String name;
    private Date date;
    private List<String> a;
}
