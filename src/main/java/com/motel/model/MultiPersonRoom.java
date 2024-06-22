package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "multi_person_room")
public class MultiPersonRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "number_of_beds")
    private Integer numberOfBeds;

    @OneToMany(mappedBy = "multiPersonRoom")
    private Set<RoomTraits> roomTraits;
}
