package com.gov.common.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor interceptor;
    public WebMvcConfig(JwtAuthInterceptor interceptor) { this.interceptor = interceptor; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/admin/**")
                .addPathPatterns("/api/v1/consultation/**")
                .addPathPatterns("/api/v1/interaction/message/**")
                .addPathPatterns("/api/v1/suggestion/**")
                .addPathPatterns("/api/v1/complaint/**")
                .addPathPatterns("/api/v1/collection/**")
                .excludePathPatterns("/api/v1/admin/auth/login", "/api/v1/admin/auth/register");
    }
}
