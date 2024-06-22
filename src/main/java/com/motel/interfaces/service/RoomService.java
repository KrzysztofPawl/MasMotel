package com.motel.interfaces.service;

import com.motel.model.Room;
import com.motel.model.enums.RoomStatus;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    @NonNull
    Optional<Room> getRoomByNumber(@NonNull int number);

    @NonNull
    Room saveRoom(@NonNull Room room);

    @NonNull
    List<Room> findRoomByStatus(@NonNull RoomStatus status);
}
