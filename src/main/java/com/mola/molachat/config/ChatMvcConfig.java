package com.mola.molachat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @Author: molamola
 * @Date: 19-8-5 下午2:42
 * @Version 1.0
 * web配置项
 */
@Configuration
public class ChatMvcConfig implements WebMvcConfigurer{

    @Autowired
    private SelfConfig config;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:"+config.getUploadFilePath()+File.separator);
    }
}
