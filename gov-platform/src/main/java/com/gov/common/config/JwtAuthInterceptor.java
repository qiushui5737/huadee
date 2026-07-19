package com.gov.common.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import com.gov.admin.service.TokenSessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;
    private final TokenSessionService tokenSessionService;
    public JwtAuthInterceptor(ObjectMapper objectMapper, TokenSessionService tokenSessionService) {
        this.objectMapper = objectMapper;
        this.tokenSessionService = tokenSessionService;
    }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        // C端公开接口无需登录
        String uri = request.getRequestURI();
        if (uri.contains("/public/")) return true;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (JwtUtil.isValid(token) && tokenSessionService.isValid(token)) {
                Claims claims = JwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                // B端管理接口需要ADMIN角色
                if (uri.startsWith("/api/v1/admin/") && !uri.startsWith("/api/v1/admin/auth/")
                        && !"ADMIN".equals(role)) {
                    response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                    return false;
                }
                // B端咨询管理接口需要ADMIN角色
                if (uri.startsWith("/api/v1/consultation/") && !"ADMIN".equals(role)) {
                    String method = request.getMethod();
                    boolean isCEnd = "GET".equals(method) && uri.matches(".*/progress/.*")
                            || ("POST".equals(method) && uri.equals("/api/v1/consultation"));
                    if (!isCEnd) {
                        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                        return false;
                    }
                }
                // 投诉管理接口：C端用户可访问POST(提交)和GET(progress)
                if (uri.startsWith("/api/v1/complaint") && !"ADMIN".equals(role)) {
                    String method = request.getMethod();
                    boolean isCEnd = ("POST".equals(method) && uri.equals("/api/v1/complaint"))
                            || ("GET".equals(method) && uri.matches(".*/progress/.*"));
                    if (!isCEnd) {
                        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                        return false;
                    }
                }
                // 建议管理接口：C端用户可访问POST(提交)和GET(progress)
                if (uri.startsWith("/api/v1/suggestion") && !"ADMIN".equals(role)) {
                    String method = request.getMethod();
                    boolean isCEnd = ("POST".equals(method) && uri.equals("/api/v1/suggestion"))
                            || ("GET".equals(method) && uri.matches(".*/progress/.*"));
                    if (!isCEnd) {
                        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                        return false;
                    }
                }
                // 政民互动留言接口：C端用户可访问GET和POST(submit/user-reply/rate)
                if (uri.startsWith("/api/v1/interaction/message") && !"ADMIN".equals(role)) {
                    String method = request.getMethod();
                    boolean isCEnd = "GET".equals(method)
                            || ("POST".equals(method) && (uri.equals("/api/v1/interaction/message")
                                    || uri.matches("/api/v1/interaction/message/\\d+/user-reply")
                                    || uri.matches("/api/v1/interaction/message/\\d+/rate")));
                    if (!isCEnd) {
                        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                        return false;
                    }
                }
                // 意见征集接口：C端用户可访问GET(public)和POST(opinion)
                if (uri.startsWith("/api/v1/collection") && !"ADMIN".equals(role)) {
                    String method = request.getMethod();
                    boolean isCEnd = "GET".equals(method)
                            || ("POST".equals(method) && uri.matches("/api/v1/collection/\\d+/opinion"));
                    if (!isCEnd) {
                        response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                        objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                        return false;
                    }
                }
                return true;
            }
        }
        response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(401, "未登录或登录已过期"));
        return false;
    }
}
