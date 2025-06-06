package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.service.UserService;
import com.backmore.secondhand_mall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // 允许前端开发服务器访问
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // 手动验证参数
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }

            User registeredUser = userService.register(user);
            
            // 生成 token
            String token = jwtUtil.generateToken(registeredUser.getUsername(), registeredUser.getId());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("user", registeredUser);
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            // 手动验证参数
            if (loginRequest == null) {
                return ResponseEntity.badRequest().body("请求参数不能为空");
            }
            if (loginRequest.get("credential") == null || loginRequest.get("credential").trim().isEmpty()) {
                return ResponseEntity.badRequest().body("登录凭证不能为空");
            }
            if (loginRequest.get("password") == null || loginRequest.get("password").trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }

            User user = userService.login(loginRequest.get("credential"), loginRequest.get("password"));
            
            // 生成 token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
