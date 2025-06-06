package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.Address;
import java.util.List;
import java.util.Optional;

public interface AddressService {
    
    /**
     * 保存收货地址
     */
    Address save(Address address);
    
    /**
     * 根据ID查找收货地址
     */
    Optional<Address> findById(Long id);
    
    /**
     * 查询用户的所有收货地址
     */
    List<Address> findByUserId(Long userId);
    
    /**
     * 查询用户的默认收货地址
     */
    Optional<Address> findDefaultByUserId(Long userId);
    
    /**
     * 设置默认收货地址
     */
    Address setDefault(Long addressId, Long userId);
    
    /**
     * 删除收货地址
     */
    void delete(Long addressId, Long userId);
    
    /**
     * 更新收货地址
     */
    Address update(Address address, Long userId);
} 