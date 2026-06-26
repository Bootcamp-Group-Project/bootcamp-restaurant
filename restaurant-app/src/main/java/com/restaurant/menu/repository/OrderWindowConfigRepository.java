package com.restaurant.menu.repository;

import com.restaurant.menu.entity.OrderWindowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderWindowConfigRepository extends JpaRepository<OrderWindowConfig, Long> {}
