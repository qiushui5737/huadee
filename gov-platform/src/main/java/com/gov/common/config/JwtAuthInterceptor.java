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
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (JwtUtil.isValid(token) && tokenSessionService.isValid(token)) {
                Claims claims = JwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                String uri = request.getRequestURI();
                boolean adminResource = (uri.startsWith("/api/v1/admin/") && !uri.startsWith("/api/v1/admin/auth/"))
                        || uri.startsWith("/api/v1/ai/admin/")
                        || uri.equals("/api/v1/ai/sensitive/words")
                        || uri.equals("/api/v1/ai/chat/audit");
                if (adminResource
                        && !"ADMIN".equals(role)) {
                    response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), Result.error(403, "无管理端访问权限"));
                    return false;
                }
                request.setAttribute("jwtToken", token);
                request.setAttribute("userId", JwtUtil.getUserId(token));
                request.setAttribute("userRole", role);
                return true;
            }
        }
        response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(401, "未登录或登录已过期"));
        return false;
    }
}
