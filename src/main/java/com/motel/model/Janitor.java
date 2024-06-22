package com.motel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "janitor")
@PrimaryKeyJoinColumn(name = "employee_id")
public class Janitor extends Employee {
    @Column(name = "tools")
    private String tools;
    @Column(name = "is_night_shift")
    private Boolean isNightShift;
}
