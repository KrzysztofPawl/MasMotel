package com.motel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "surname")
    private String surname;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "email")
    private String email;

    @Column(name = "pesel", unique = true)
    @Size(min = 11, max = 11, message = "PESEL must be exactly 11 digits long")
    @Pattern(regexp = "^[0-9]{11}$", message = "PESEL must contain only digits")
    private String pesel;
}
