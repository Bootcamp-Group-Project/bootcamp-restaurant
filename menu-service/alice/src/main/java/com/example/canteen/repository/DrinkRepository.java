package com.example.canteen.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.canteen.entity.Drink;
import com.example.canteen.entity.Status;

public interface DrinkRepository extends JpaRepository<Drink, Long> {

    List<Drink> findByStatus(Status status);

    Optional<Drink> findByCode(String code);

    @Query("SELECT d FROM Drink d WHERE d.status = 'ON_LIST' AND d.balance > 0")
    List<Drink> findAllAvailable();
}
