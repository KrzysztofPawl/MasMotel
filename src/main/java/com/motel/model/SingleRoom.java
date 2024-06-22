package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "single_room")
public class SingleRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bed_size")
    private Integer bedSize;

    @OneToMany(mappedBy = "singleRoom")
    private Set<RoomTraits> roomTraits;
}
