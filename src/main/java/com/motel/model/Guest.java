package com.motel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "guest")
@Inheritance(strategy = InheritanceType.JOINED)
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @OneToMany(mappedBy = "guest")
    private Set<Reservation> reservations;

    @Column(name = "registration_plate_number")
    @Size(min = 5, max = 7, message = "Registration plate number must be between 5 and 7 characters long")
    private String registrationPlateNumber;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    public void softDelete() {
        this.isDeleted = true;
    }
}
