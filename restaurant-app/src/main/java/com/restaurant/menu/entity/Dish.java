package com.restaurant.menu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.restaurant.menu.util.StockStatusUtil;

@Entity
@Table(name = "dishes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Dish {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "special_request_options", length = 500)
    private String specialRequestOptions;

    @Column(nullable = false)
    private Integer initialStock;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false)
    private LocalDate menuDate;

    @Column(length = 20, columnDefinition = "varchar(20) default 'NORMAL'")
    private String category = "NORMAL";

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean published = false;

    @Column(nullable = false)
    private Integer preparationTime;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ON_LIST;

    @ManyToMany(mappedBy = "dishes")
    private Set<Menu> menus = new HashSet<>();

    @PrePersist @PreUpdate
    void refreshStatus() {
        this.status = StockStatusUtil.syncStatus(this.balance);
    }
}
