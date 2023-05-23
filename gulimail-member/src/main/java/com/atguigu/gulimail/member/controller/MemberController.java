package com.atguigu.gulimail.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.utils.BusinessCode;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.gulimail.member.exception.PhoneExistException;
import com.atguigu.gulimail.member.exception.UserNameExistException;
import com.atguigu.gulimail.member.vo.LoginVo;
import com.atguigu.gulimail.member.vo.RegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimail.member.entity.MemberEntity;
import com.atguigu.gulimail.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:05:52
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
        memberService.save(member);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/regist")
    public R regist(@RequestBody RegistVo vo){
        try {
            memberService.regist(vo);
            log.info("注册成功："+vo);
            return R.ok();
        } catch (PhoneExistException e) {
            log.error("注册失败："+e.getMessage());
            return R.error(e.getMessage());
        }catch (UserNameExistException e) {
            log.error("注册失败："+e.getMessage());
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginVo vo){
        MemberEntity user = memberService.login(vo);
        if (user==null){
            log.info("登录失败,用户名不存在："+vo);
            return R.error(BusinessCode.LOGINERRORNONE.getCode(),BusinessCode.LOGINERRORNONE.getMessage());
        }
        if (StringUtils.isEmpty(user.getUsername())){
            log.info("登录失败："+vo);
            return R.error(BusinessCode.LOGINERROR.getCode(),BusinessCode.LOGINERROR.getMessage());
        }
        log.info("登录成功："+vo);
        MemberVO memberVO = new MemberVO();
        BeanUtils.copyProperties(user,memberVO);
        return R.ok().put("user",memberVO);
    }
}
