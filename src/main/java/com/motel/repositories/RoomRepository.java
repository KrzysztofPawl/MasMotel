package com.motel.repositories;

import com.motel.model.Room;
import com.motel.model.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    @NonNull
    Optional<Room> findByNumber(@NonNull int number);

    @NonNull
    Room save(@NonNull Room room);

    @NonNull
    List<Room> findByStatus(@NonNull RoomStatus status);

}