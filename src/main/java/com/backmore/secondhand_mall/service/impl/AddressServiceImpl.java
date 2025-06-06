package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.entity.Address;
import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.repository.AddressRepository;
import com.backmore.secondhand_mall.repository.UserRepository;
import com.backmore.secondhand_mall.service.AddressService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public Address save(Address address) {
        // 如果是默认地址，则将该用户的其他地址设为非默认
        if (address.getIsDefault()) {
            addressRepository.resetDefaultAddresses(address.getUser());
        }
        // 如果是用户的第一个地址，则设为默认地址
        else if (addressRepository.countByUser(address.getUser()) == 0) {
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }
    
    @Override
    public Optional<Address> findById(Long id) {
        return addressRepository.findById(id);
    }
    
    @Override
    public List<Address> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
    }
    
    @Override
    public Optional<Address> findDefaultByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }
    
    @Override
    @Transactional
    public Address setDefault(Long addressId, Long userId) {
        // 验证地址存在且属于该用户
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此地址");
        }
        
        // 将该用户的所有地址设为非默认
        User user = address.getUser();
        addressRepository.resetDefaultAddresses(user);
        
        // 将当前地址设为默认
        address.setIsDefault(true);
        return addressRepository.save(address);
    }
    
    @Override
    @Transactional
    public void delete(Long addressId, Long userId) {
        // 验证地址存在且属于该用户
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此地址");
        }
        
        addressRepository.delete(address);
        
        // 如果删除的是默认地址，且用户还有其他地址，则将第一个地址设为默认
        if (address.getIsDefault()) {
            User user = address.getUser();
            List<Address> remainingAddresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remainingAddresses.isEmpty()) {
                Address firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                addressRepository.save(firstAddress);
            }
        }
    }
    
    @Override
    @Transactional
    public Address update(Address updatedAddress, Long userId) {
        try {
            // 验证地址存在且属于该用户
            if (updatedAddress.getId() == null) {
                throw new RuntimeException("地址ID不能为空");
            }
            
            Address existingAddress = addressRepository.findById(updatedAddress.getId())
                    .orElseThrow(() -> new RuntimeException("地址不存在"));
            
            if (existingAddress.getUser() == null || existingAddress.getUser().getId() == null) {
                throw new RuntimeException("地址没有关联用户");
            }
            
            if (!existingAddress.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权操作此地址");
            }
            
            // 确保用户信息不被覆盖
            if (updatedAddress.getUser() == null || updatedAddress.getUser().getId() == null) {
                updatedAddress.setUser(existingAddress.getUser());
            }
            
            // 调试日志
            System.out.println("服务层更新地址: ID=" + updatedAddress.getId());
            System.out.println("原地址: 收件人=" + existingAddress.getRecipientName() + ", 电话=" + existingAddress.getRecipientPhone());
            System.out.println("新地址: 收件人=" + updatedAddress.getRecipientName() + ", 电话=" + updatedAddress.getRecipientPhone());
            
            // 更新地址信息
            existingAddress.setRecipientName(updatedAddress.getRecipientName());
            existingAddress.setRecipientPhone(updatedAddress.getRecipientPhone());
            existingAddress.setAddress(updatedAddress.getAddress());
            
            // 如果设置为默认地址
            if (updatedAddress.getIsDefault() && !existingAddress.getIsDefault()) {
                addressRepository.resetDefaultAddresses(existingAddress.getUser());
                existingAddress.setIsDefault(true);
            }
            
            return addressRepository.save(existingAddress);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
} 