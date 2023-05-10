package com.atguigu.gulimail.search.service;

import com.atguigu.gulimail.search.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    User save(User user);
    boolean del(Integer id);
    User update(User user);
    List<User> selectList(Map map);
}
