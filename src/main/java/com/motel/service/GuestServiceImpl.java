package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.GuestService;
import com.motel.model.Guest;
import com.motel.repositories.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    public Guest saveGuest(Guest guest) {
        log.info("Guest saved: {}", guest);
        return guestRepository.save(guest);
    }

    @Override
    public Guest getGuestById(int id) {
        return null;
    }

    @Override
    public Guest getGuestByPesel(String pesel) {
        var result = guestRepository.findGuestByPersonPesel(pesel).orElse(null);

        if (result == null) {
            log.warn("Guest not found: {}", pesel);
            throw new DataNotFoundException("Guest not found: " + pesel);
        }
        log.info("Guest found: {}", result);
        return result;
    }

    @Override
    public void deleteGuest(int id) {

    }

    @Override
    public boolean existsByPesel(String pesel) {
        return guestRepository.existsByPersonPesel(pesel);
    }

    @Override
    public void updateGuestRegistrationPlateNumber(String pesel, String registrationPlateNumber) {
        Guest guest = guestRepository.findGuestByPersonPesel(pesel)
                .orElseThrow(() -> new DataNotFoundException("Guest not found: " + pesel));
        guest.setRegistrationPlateNumber(registrationPlateNumber);
        guestRepository.save(guest);
        log.info("Guest registration plate number updated: {}", guest);
    }

    @Override
    public Optional<String> findRegistrationPlateNumberByPesel(@NonNull String pesel) {
        return guestRepository.findGuestByPersonPesel(pesel)
                .map(Guest::getRegistrationPlateNumber);
    }


}
