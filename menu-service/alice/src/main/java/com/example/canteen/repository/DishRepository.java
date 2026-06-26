package com.example.canteen.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Status;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByMenuDate(LocalDate menuDate);

    List<Dish> findByMenuDateAndStatus(LocalDate menuDate, Status status);

    Optional<Dish> findByMenuDateAndCode(LocalDate menuDate, String code);

    @Query("SELECT d FROM Dish d WHERE d.menuDate = :date AND d.balance > 0 AND d.status = 'ON_LIST'")
    List<Dish> findTodayAvailable(@Param("date") LocalDate date);
}
