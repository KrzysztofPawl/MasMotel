package com.motel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "manager")
@PrimaryKeyJoinColumn(name = "employee_id")
public class Manager extends Employee {
    @Column(name = "department")
    private String department;
    @Column(name = "bonus")
    private BigDecimal bonus;
}
