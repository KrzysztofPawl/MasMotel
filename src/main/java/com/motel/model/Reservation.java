package com.motel.model;

import com.motel.model.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pesel")
    @Size(min = 11, max = 11, message = "PESEL must be exactly 11 digits long")
    @Pattern(regexp = "^[0-9]{11}$", message = "PESEL must contain only digits")
    private String pesel;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER)
    private Set<Room> rooms;

    @OneToMany(mappedBy = "reservation")
    private Set<ServiceOrder> serviceOrders;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Invoice> invoices;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @Column(name = "nights")
    private Integer nights;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @PrePersist
    @PreUpdate
    private void calculateNights() {
        if (arrivalDate != null && leaveDate != null) {
            this.nights = (int) ChronoUnit.DAYS.between(arrivalDate, leaveDate);
        } else {
            throw new IllegalArgumentException("Arrival and leave dates must be set");
        }
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.status = ReservationStatus.DELETED;
    }

    public Invoice getInvoice() {
        return invoices.stream().findFirst().orElse(null);
    }

}
