package com.restaurant.menu.repository;

import com.restaurant.menu.entity.Drink;
import com.restaurant.menu.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DrinkRepository extends JpaRepository<Drink, Long> {
    List<Drink> findByStatus(Status status);
    Optional<Drink> findByCode(String code);

    @Query("SELECT d FROM Drink d WHERE d.status = 'ON_LIST' AND d.balance > 0")
    List<Drink> findAllAvailable();
}
