package com.restaurant.menu.repository;

import com.restaurant.menu.entity.Menu;
import com.restaurant.menu.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByMenuDate(LocalDate menuDate);
    List<Menu> findByMenuDateAndStatus(LocalDate menuDate, Status status);
    Optional<Menu> findByMenuDateAndName(LocalDate menuDate, String name);

    @Query("SELECT m FROM Menu m WHERE m.menuDate = :date AND m.balance > 0 AND m.status = 'ON_LIST'")
    List<Menu> findTodayAvailable(@Param("date") LocalDate date);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.dishes LEFT JOIN FETCH m.drinks WHERE m.id = :id")
    Optional<Menu> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Menu m LEFT JOIN FETCH m.dishes LEFT JOIN FETCH m.drinks"
            + " WHERE m.menuDate = :date AND m.balance > 0 AND m.status = 'ON_LIST'")
    List<Menu> findTodayAvailableWithItems(@Param("date") LocalDate date);
}
