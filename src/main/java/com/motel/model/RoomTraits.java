package com.motel.model;

import com.motel.exception.IllegalRoomConfigurationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "room_traits")
public class RoomTraits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "roomTraits")
    private Set<Room> rooms;

    @ManyToOne
    @JoinColumn(name = "multi_person_room_id")
    private MultiPersonRoom multiPersonRoom;

    @ManyToOne
    @JoinColumn(name = "single_room_id")
    private SingleRoom singleRoom;

    @ManyToOne
    @JoinColumn(name = "standard_room_id")
    private StandardRoom standardRoom;

    @ManyToOne
    @JoinColumn(name = "premium_room_id")
    private PremiumRoom premiumRoom;

    @PrePersist
    @PreUpdate
    private void validateRoomConfiguration() {
        if(multiPersonRoom != null && singleRoom != null) {
            throw new IllegalRoomConfigurationException("Room cannot be both multi person and single person");
        }
        if(premiumRoom != null && standardRoom != null) {
            throw new IllegalRoomConfigurationException("Room cannot be both premium and standard");
        }
        if(multiPersonRoom == null && singleRoom == null && standardRoom == null && premiumRoom == null) {
            throw new IllegalRoomConfigurationException("Room must be either multi person, single person, standard or premium");
        }
    }
}