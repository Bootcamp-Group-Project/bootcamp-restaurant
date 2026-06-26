package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.restaurant.menu.util.StockStatusUtil;

@Entity
@Table(name = "menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Menu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate menuDate;

    @Column(nullable = false)
    private LocalTime startTime = LocalTime.of(11, 0);

    @Column(nullable = false)
    private LocalTime endTime = LocalTime.of(14, 30);

    @Column(nullable = false)
    private Integer initialStock;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ON_LIST;

    @ManyToMany
    @JoinTable(
            name = "menu_dishes",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id"))
    private Set<Dish> dishes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "menu_drinks",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "drink_id"))
    private Set<Drink> drinks = new HashSet<>();

    @PrePersist @PreUpdate
    void refreshStatus() {
        this.status = StockStatusUtil.syncStatus(this.balance);
    }
}
