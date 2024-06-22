package com.motel.interfaces.service;

import com.motel.model.Guest;

import java.util.Optional;

public interface GuestService {
    Guest saveGuest(Guest guest);
    Guest getGuestById(int id);
    Guest getGuestByPesel(String pesel);
    void deleteGuest(int id);
    boolean isGuestDeleted(int id);

    boolean existsByPesel(String pesel);

    void updateGuestRegistrationPlateNumber(String pesel, String registrationPlateNumber);

    Optional<String> findRegistrationPlateNumberByPesel(String pesel);
}
