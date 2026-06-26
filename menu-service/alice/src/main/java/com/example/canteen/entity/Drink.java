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
import java.util.HashSet;
import java.util.Set;
import com.example.canteen.util.StockStatusUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drinks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Drink {

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

    @Column(name = "special_request_options", length = 500)
    private String specialRequestOptions;

    @Column(nullable = false)
    private Integer balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ON_LIST;

    @ManyToMany(mappedBy = "drinks")
    private Set<Menu> menus = new HashSet<>();

    @PrePersist
    @PreUpdate
    void refreshStatus() {
        this.status = StockStatusUtil.syncStatus(this.balance);
    }
}
