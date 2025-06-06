package com.backmore.secondhand_mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 创建上传目录
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                boolean created = uploadDirFile.mkdirs();
                System.out.println("创建目录结果: " + created + ", 路径: " + uploadDirFile.getAbsolutePath());
            }
            
            // 获取绝对路径
            String uploadAbsolutePath = uploadDirFile.getAbsolutePath();
            System.out.println("上传目录绝对路径: " + uploadAbsolutePath);
            
            // 确保路径以斜杠结尾
            if (!uploadAbsolutePath.endsWith("/") && !uploadAbsolutePath.endsWith("\\")) {
                uploadAbsolutePath += File.separator;
            }
            
            // 配置静态资源映射
            String fileUrl = "file:" + uploadAbsolutePath;
            System.out.println("文件URL: " + fileUrl);
            
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(fileUrl);
            
            // 添加更多的静态资源映射
            registry.addResourceHandler("/static/**")
                    .addResourceLocations("classpath:/static/");
                    
            System.out.println("静态资源配置完成");
        } catch (Exception e) {
            System.err.println("配置静态资源时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 