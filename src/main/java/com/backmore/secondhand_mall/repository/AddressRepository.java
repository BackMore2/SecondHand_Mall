package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Address;
import com.backmore.secondhand_mall.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // 查询用户的所有地址
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    // 查询用户的默认地址
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    
    // 取消所有默认地址
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDefault = true")
    void resetDefaultAddresses(User user);
    
    // 统计用户地址数量
    long countByUser(User user);
} 