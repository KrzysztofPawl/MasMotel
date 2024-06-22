package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "standard_room")
public class StandardRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_size")
    private Integer roomSize;

    @OneToMany(mappedBy = "standardRoom")
    private Set<RoomTraits> roomTraits;

}