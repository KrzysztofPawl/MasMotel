package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.RoomService;
import com.motel.model.Room;
import com.motel.model.enums.RoomStatus;
import com.motel.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    @NonNull
    public Optional<Room> getRoomByNumber(int number) {
        var result = roomRepository.findByNumber(number);
        if (result.isEmpty()) {
            log.warn("Room not found: {}", number);
            throw new DataNotFoundException("Room not found: " + number);
        }
        log.info("Room found: {}", result);
        return result;
    }

    @Override
    @NonNull
    public Room saveRoom(@NonNull Room room) {
        return roomRepository.save(room);
    }

    @Override
    @NonNull
    public List<Room> findRoomByStatus(@NonNull RoomStatus status) {
        var result = roomRepository.findByStatus(status);
        if (result.isEmpty()) {
            log.warn("Rooms not found with status: {}", status);
            throw new DataNotFoundException("Rooms not found with status: " + status);
        }
        log.info("Rooms found: {}", result);
        return result;
    }
}
