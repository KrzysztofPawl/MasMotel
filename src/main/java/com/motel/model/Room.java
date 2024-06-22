package com.motel.model;

import com.motel.model.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = true)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "room_traits_id", nullable = false)
    private RoomTraits roomTraits;

    @Column(name = "number")
    private Integer number;

    @Column(name = "price_for_night")
    private BigDecimal priceForNight;

    @Column(name = "floor")
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;
}
