package com.canteen.demo_canteen.dto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.canteen.demo_canteen.entity.BaseUser;

@Repository
public interface BaseUserRepository extends JpaRepository<BaseUser, Long> {
    BaseUser findByPhone(String phone);
    BaseUser findByPhoneAndRole(String phone, String role);
}
