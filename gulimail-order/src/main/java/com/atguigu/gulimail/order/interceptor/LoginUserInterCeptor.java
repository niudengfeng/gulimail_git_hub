package com.atguigu.gulimail.order.interceptor;

import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.common.vo.UserInfo;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginUserInterCeptor implements HandlerInterceptor {

    public static ThreadLocal<MemberVO> threadLocal = new ThreadLocal<>();

    /**
     * 检测是否登录,订单系统必须拦截所有请求,必须登录
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
        if (merber==null){
            session.setAttribute("msg","请先登录!");
            response.sendRedirect("http://auth.gulimail.com/login.html");
            return false;
        }
        threadLocal.set(merber);
        return true;
    }
}
