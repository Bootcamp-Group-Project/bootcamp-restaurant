package com.restaurant.login.repository;

import com.restaurant.login.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseUserRepository extends JpaRepository<BaseUser, Long> {
    BaseUser findByPhone(String phone);
    BaseUser findByPhoneAndRole(String phone, String role);
}
