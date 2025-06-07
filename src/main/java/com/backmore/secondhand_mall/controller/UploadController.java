package com.backmore.secondhand_mall.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        logger.info("收到图片上传请求, 文件大小: {} bytes", file.getSize());
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                logger.warn("上传的文件为空");
                return ResponseEntity.badRequest().body(Map.of("error", "请选择一个文件"));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("上传的文件不是图片类型: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "只能上传图片文件"));
            }

            logger.info("上传图片类型: {}", contentType);

            // 创建上传目录
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                boolean dirCreated = uploadDirFile.mkdirs();
                if (!dirCreated) {
                    logger.error("无法创建目录: {}", uploadDir);
                    return ResponseEntity.status(500).body(Map.of("error", "无法创建上传目录"));
                }
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = "product_" + UUID.randomUUID() + fileExtension;
            
            // 保存文件
            Path targetLocation = Paths.get(uploadDir).resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation);
            
            logger.info("图片保存成功: {}", targetLocation.toString());
            
            // 将图片转换为 Base64 编码
            byte[] fileContent = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileContent);
            String dataUrl = "data:" + contentType + ";base64," + base64Image;
            
            // 返回图片URL
            Map<String, String> response = new HashMap<>();
            response.put("url", dataUrl);
            
            logger.info("返回Base64图片数据 (长度: {})", dataUrl.length());
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("图片上传失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("处理图片上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/upload/base64")
    public ResponseEntity<?> uploadBase64(@RequestBody Map<String, Object> request) {
        try {
            logger.info("收到Base64图片上传请求");
            
            // 从请求中获取图片数据
            Object imageObj = request.get("image");
            if (imageObj == null) {
                logger.warn("请求中没有图片数据");
                return ResponseEntity.badRequest().body(Map.of("error", "请提供base64图片数据"));
            }
            
            String base64String;
            if (imageObj instanceof String) {
                base64String = (String) imageObj;
                logger.info("收到字符串类型的图片数据，长度: {}", base64String.length());
            } else if (imageObj instanceof List) {
                // 如果是图片列表，直接返回
                logger.info("收到列表类型的图片数据");
                return ResponseEntity.ok(Map.of("url", imageObj));
            } else {
                logger.warn("不支持的图片数据类型: {}", imageObj.getClass().getName());
                return ResponseEntity.badRequest().body(Map.of("error", "不支持的图片数据格式"));
            }
            
            // 如果前端直接发送的是JSON字符串形式的图片列表，需要特殊处理
            if (base64String.startsWith("[") && base64String.endsWith("]")) {
                logger.info("收到JSON格式的图片列表");
                return ResponseEntity.ok(Map.of("url", base64String));
            }
            
            // 返回URL (实际上就是原始的base64字符串)
            Map<String, String> response = new HashMap<>();
            response.put("url", base64String);
            
            logger.info("成功处理Base64图片数据");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("处理Base64图片上传请求时发生异常", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 