package com.example.canteen.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import com.example.canteen.util.StockStatusUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    /** 逗號分隔或 JSON，例如：少飯,少油,加辣 */
    @Column(name = "special_request_options", length = 500)
    private String specialRequestOptions;

    @Column(nullable = false)
    private Integer initialStock;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false)
    private LocalDate menuDate;

    /** 新增：出餐預估時間（分鐘） */
    @Column(nullable = false)
    private Integer preparationTime;     // ← 新增這欄

    @Enumerated(EnumType.STRING)
    private Status status = Status.ON_LIST;

    @ManyToMany(mappedBy = "dishes")
    private Set<Menu> menus = new HashSet<>();

}
