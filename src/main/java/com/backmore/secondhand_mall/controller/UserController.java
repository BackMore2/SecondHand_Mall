package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
public class UserController {

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.findAll(pageable);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@RequestBody User user) {
        return userService.update(user);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> profileData) {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            // 验证权限：只有管理员或用户本人可以修改资料
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权修改其他用户的资料"));
            }

            // 更新个人资料
            if (profileData.containsKey("username")) {
                // 检查用户名是否已存在
                Optional<User> existingUser = userService.findByUsername(profileData.get("username"));
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
                }
                user.setUsername(profileData.get("username"));
            }
            if (profileData.containsKey("email")) {
                user.setEmail(profileData.get("email"));
            }
            if (profileData.containsKey("phone")) {
                user.setPhone(profileData.get("phone"));
            }

            User updatedUser = userService.update(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            // 验证权限：只有管理员或用户本人可以修改头像
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权修改其他用户的头像"));
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "请选择一个文件"));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "只能上传图片文件"));
            }

            // 创建上传目录
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                boolean dirCreated = uploadDirFile.mkdirs();
                if (!dirCreated) {
                    System.out.println("无法创建目录: " + uploadDir);
                    return ResponseEntity.status(500).body(Map.of("error", "无法创建上传目录"));
                }
            }
            
            // 输出上传目录的绝对路径，用于调试
            System.out.println("上传目录的绝对路径: " + uploadDirFile.getAbsolutePath());

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = "avatar_" + id + "_" + UUID.randomUUID() + fileExtension;
            
            // 保存文件
            Path targetLocation = Paths.get(uploadDir).resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation);
            
            // 输出文件保存路径，用于调试
            System.out.println("文件保存路径: " + targetLocation.toAbsolutePath());
            
            // 将图片转换为 Base64 编码
            byte[] fileContent = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileContent);
            String dataUrl = "data:" + contentType + ";base64," + base64Image;
            
            // 更新用户头像路径 - 使用 Base64 编码
            user.setAvatar(dataUrl);
            User updatedUser = userService.update(user);
            
            // 输出头像URL，用于调试
            System.out.println("头像数据URL长度: " + dataUrl.length());
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResponseEntity.badRequest().body(Map.of("error", "文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误堆栈
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData) {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            // 验证权限：只有管理员或用户本人可以修改密码
            if (!isAdmin && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).body(Map.of("error", "无权修改其他用户的密码"));
            }

            // 验证参数
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "旧密码和新密码不能为空"));
            }
            
            // 验证旧密码是否正确
            if (!userService.checkPassword(user, oldPassword)) {
                return ResponseEntity.status(400).body(Map.of("error", "旧密码不正确"));
            }
            
            // 更新密码
            userService.updatePassword(user, newPassword);
            
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
