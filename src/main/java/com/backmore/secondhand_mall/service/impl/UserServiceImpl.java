package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.repository.UserRepository;
import com.backmore.secondhand_mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public User register(User user) {
        // 检查用户名唯一
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(true); // 默认正常
        user.setIsAdmin(false); // 默认普通用户
        return userRepository.save(user);
    }

    @Override
    public User login(String credential, String password) {
        if (credential == null || password == null) {
            throw new RuntimeException("登录凭证和密码不能为空");
        }

        Optional<User> userOpt;
        
        // 判断是否是邮箱格式
        if (credential.contains("@")) {
            userOpt = userRepository.findByEmail(credential);
        } else {
            // 如果不是邮箱，则视为手机号
            userOpt = userRepository.findByPhone(credential);
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public boolean checkPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
