package com.motel.model;

import com.motel.model.enums.AdditionalServiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "additional_service")
public class AdditionalService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "additionalService")
    private Set<ServiceOrder> serviceOrders;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdditionalServiceStatus status;
}
