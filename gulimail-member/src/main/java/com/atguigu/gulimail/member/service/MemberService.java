package com.atguigu.gulimail.member.service;

import com.atguigu.gulimail.member.vo.LoginVo;
import com.atguigu.gulimail.member.vo.RegistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:05:52
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(RegistVo vo);

    MemberEntity login(LoginVo vo);
}

