package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employee")
@Inheritance(strategy = InheritanceType.JOINED)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "position")
    private String position;
    @Column(name = "seniority")
    private String seniority;
    @Column(name = "salary")
    private BigDecimal salary;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "age")
    private Integer age;

}