package com.example.canteen.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.canteen.entity.Menu;
import com.example.canteen.entity.Status;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    //check menu list
    List<Menu> findByMenuDate(LocalDate menuDate);

    // check dish status (on list/sold out) on that date
    List<Menu> findByMenuDateAndStatus(LocalDate menuDate, Status status);

    //
    Optional<Menu> findByMenuDateAndName(LocalDate menuDate, String name);

    // only can see the "on list" menu
    @Query("SELECT m FROM Menu m WHERE m.menuDate = :date AND m.balance > 0 AND m.status = 'ON_LIST'")
    List<Menu> findTodayAvailable(@Param("date") LocalDate date);

    // take out all select items (dishes and drinks)
    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.dishes LEFT JOIN FETCH m.drinks WHERE m.id = :id")
    Optional<Menu> findByIdWithItems(@Param("id") Long id);

    //
    @Query(
            "SELECT DISTINCT m FROM Menu m LEFT JOIN FETCH m.dishes LEFT JOIN FETCH m.drinks"
                    + " WHERE m.menuDate = :date AND m.balance > 0 AND m.status = 'ON_LIST'")
    List<Menu> findTodayAvailableWithItems(@Param("date") LocalDate date);
}
