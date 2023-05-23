package com.atguigu.gulimail.member.dao;

import com.atguigu.gulimail.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:05:52
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    int checkPhoneExist(@Param("phone") String phone);
    int checkUserNameExist(@Param("userName") String userName);
}
