package com.hkd.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    // 1. 定义成员变量
    @Resource
    private  TokenUtil tokenUtil;
    @Resource
    private RedisUtil redisUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取 Token
        String token = request.getSession().getAttribute("accessToken").toString();
        String refreshToken=TokenUtil.REFRESH_TOKEN_PREFIX+request.getSession().getAttribute("refreshToken");
        //从会话中获得token
//
//        // 2. 没有 Token
        if (token == null) {
            response.setStatus(401);
            response.getWriter().write("未登录，请先登录");
            return false;
        }
//
//        // 3. 在黑名单（登出/强制下线）
        if (redisUtil.isBlackList(token)) {
            response.setStatus(401);
            response.getWriter().write("Token 已失效，请重新登录");
            return false;
        }

        // 4. Token 无效时，但是还想保持登录状态
        if (!tokenUtil.validateToken(token)) {
            if(redisUtil.exists(refreshToken)){
                String accessToken = tokenUtil.refresh(refreshToken);
                if(accessToken != null) {
                    boolean valid = tokenUtil.validateToken(accessToken);
                    if (valid) {
                        request.getSession().setAttribute("accessToken",accessToken);
                        response.getWriter().write("Token 刷新成功！");
                        return true;
                    }
                }
            }else{
                response.setStatus(401);
                response.getWriter().write("refreshToken 无效或已过期");
            }
            return false;
        }
         //5. 全部验证通过 → 放行
        return true;
    }
}