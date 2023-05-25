package com.atguigu.gulimail.member.service.impl;

import com.atguigu.common.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.member.dao.MemberReceiveAddressDao;
import com.atguigu.gulimail.member.entity.MemberReceiveAddressEntity;
import com.atguigu.gulimail.member.service.MemberReceiveAddressService;
import org.springframework.util.CollectionUtils;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<MemberAddressVo> getListByMemberId(Long memberId) {
        QueryWrapper<MemberReceiveAddressEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("member_id",memberId);
        List<MemberReceiveAddressEntity> selectedList = this.getBaseMapper().selectList(wrapper);
        if (!CollectionUtils.isEmpty(selectedList)){
            List<MemberAddressVo> collect = selectedList.stream().map(d -> {
                MemberAddressVo memberAddressVo = new MemberAddressVo();
                BeanUtils.copyProperties(d,memberAddressVo);
                return memberAddressVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}
