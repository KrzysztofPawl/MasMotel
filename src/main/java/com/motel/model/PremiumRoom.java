package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "premium_room")
public class PremiumRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_size")
    private Integer roomSize;

    @Column(name = "free_standing_bathtub_size")
    private Integer freeStandingBathtubSize;

    @OneToMany(mappedBy = "premiumRoom")
    private Set<RoomTraits> roomTraits;
}
