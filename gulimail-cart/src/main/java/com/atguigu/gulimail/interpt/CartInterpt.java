package com.atguigu.gulimail.interpt;

import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.common.vo.UserInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

public class CartInterpt implements HandlerInterceptor {

    public static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     * 1.判断请求是否登录
     * 2.分配临时userKey
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberVO merber = (MemberVO) session.getAttribute(RedisConstants.SESSION_USER_KEY);
        UserInfo userInfo = new UserInfo();
        if (merber!=null){
            //登录成功的
            userInfo.setUserId(merber.getId());
            userInfo.setUserName(merber.getUsername());
            userInfo.setPhone(merber.getMobile());
        }

        /**
         * 从当前请求里面获取cookie,判断是否存在user-key了
         */
        Cookie[] cookies = request.getCookies();
        if (cookies!=null && cookies.length>0){
            for (Cookie cookie : cookies){
                String name = cookie.getName();
                if (RedisConstants.SESSION_USER_KEY_NAME.equals(name)){
                    //存在，把这个user-key放进userInfo中,防止每次是新产生的 。只要添加过一次，就不需要再放新的userKey了
                    userInfo.setUserKeyCookie(cookie.getValue());
                    userInfo.setTemFlag(true);//添加过
                }
            }
        }

        if (StringUtils.isEmpty(userInfo.getUserKeyCookie())){
            //如果没有值，给他分配个
            String uuid = UUID.randomUUID().toString();
            userInfo.setUserKeyCookie(uuid);
        }

        threadLocal.set(userInfo);//同一个线程内是共享的
        return true;
    }

    /**
     * 目标方法执行后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfo userInfo = threadLocal.get();
        if (!userInfo.isTemFlag()){
            //说明这次请求没有登录，给这个临时用户返回个cookie，下次
            Cookie cookie = new Cookie(RedisConstants.SESSION_USER_KEY_NAME,userInfo.getUserKeyCookie());
            cookie.setDomain("gulimail.com");
            cookie.setComment("购物车服务的用户cookie");
            cookie.setMaxAge(RedisConstants.SESSION_USER_KEY_TIMEOUT);//设置最大失效时间
            response.addCookie(cookie);
        }
    }
}
