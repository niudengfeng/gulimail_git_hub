package com.atguigu.gulimail.member.service.impl;

import com.atguigu.gulimail.member.dao.MemberLevelDao;
import com.atguigu.gulimail.member.entity.MemberLevelEntity;
import com.atguigu.gulimail.member.exception.PhoneExistException;
import com.atguigu.gulimail.member.exception.UserNameExistException;
import com.atguigu.gulimail.member.service.MemberLevelService;
import com.atguigu.gulimail.member.vo.LoginVo;
import com.atguigu.gulimail.member.vo.RegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.member.dao.MemberDao;
import com.atguigu.gulimail.member.entity.MemberEntity;
import com.atguigu.gulimail.member.service.MemberService;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService levelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(RegistVo vo){
        /**
         * 需要先校验当前用户名和手机号库里是否存在
         */
        checkPhoneExist(vo.getPhone());
        checkUserNameExist(vo.getUserName());

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setCreateTime(new Date());
        //获取会员默认等级
        MemberLevelEntity defaultLevel = levelService.getDefaultLevel();
        memberEntity.setLevelId(defaultLevel.getId());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setNickname(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        //TODO 需要加密处理 用spring提供的密码盐值加密器
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));
        this.save(memberEntity);
    }

    @Override
    public MemberEntity login(LoginVo vo) {
        //1.先查询当前用户的数据库加密后密码
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",vo.getUserName()).or().eq("mobile",vo.getUserName());
        MemberEntity memberEntity = this.baseMapper.selectOne(queryWrapper);
        if (memberEntity==null){
            log.error("当前用户不存在："+vo.getUserName());
            return null;
        }
        String password = memberEntity.getPassword();//加密后
        String in = vo.getPassword();//用户输入的比如123456
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (bCryptPasswordEncoder.matches(in,password)) {
            //登录成功
            return memberEntity;
        }else {
            //登录失败密码错误
            log.error("登录失败，用户名密码不匹配："+vo);
            return new MemberEntity();
        }
    }

    private void checkPhoneExist(String phone) {
        int count = this.baseMapper.checkPhoneExist(phone);
        if (count>0){
            throw new PhoneExistException();
        }
    }
    private void checkUserNameExist(String userName) {
        int count = this.baseMapper.checkUserNameExist(userName);
        if (count>0){
            throw new UserNameExistException();
        }
    }
}
