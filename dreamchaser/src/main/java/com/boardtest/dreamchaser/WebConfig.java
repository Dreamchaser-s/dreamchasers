package com.boardtest.dreamchaser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {


        String resourcePath = "file:///" + uploadPath;


        System.out.println("--- DEBUG: Resource Handler Path Mapped ---");
        System.out.println("/uploads/** -> " + resourcePath);
        System.out.println("-------------------------------------------");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);
    }
}