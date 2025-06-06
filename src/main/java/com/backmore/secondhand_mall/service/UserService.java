package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserService {
    User save(User user);
    void deleteById(Long id);
    Optional<User> findById(Long id);
    Page<User> findAll(Pageable pageable);
    User update(User user);

    // 新增注册和登录方法
    User register(User user);
    User login(String credential, String password);
    Optional<User> findByUsername(String username);
    
    // 密码相关方法
    boolean checkPassword(User user, String password);
    void updatePassword(User user, String newPassword);
}
