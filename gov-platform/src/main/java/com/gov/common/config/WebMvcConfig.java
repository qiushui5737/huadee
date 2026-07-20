package com.gov.common.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor interceptor;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public WebMvcConfig(JwtAuthInterceptor interceptor) { this.interceptor = interceptor; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/admin/**")
                .addPathPatterns("/api/v1/consultation/**")
                .addPathPatterns("/api/v1/interaction/message/**")
                .addPathPatterns("/api/v1/suggestion/**")
                .addPathPatterns("/api/v1/complaint/**")
                .addPathPatterns("/api/v1/collection/**")
                .addPathPatterns("/api/v1/service/catalog/my-items", "/api/v1/service/catalog/items/**")
                .addPathPatterns("/api/v1/service/payment/**", "/api/v1/service/form/submit", "/api/v1/service/form/upload", "/api/v1/service/form/drafts", "/api/v1/service/form/drafts/**", "/api/v1/service/form/rating", "/api/v1/service/form/ratings")
                .excludePathPatterns("/api/v1/admin/auth/login", "/api/v1/admin/auth/register", "/api/v1/service/catalog/categories", "/api/v1/service/catalog/items");
    }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
