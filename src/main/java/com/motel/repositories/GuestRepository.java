package com.motel.repositories;

import com.motel.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Integer> {
    @NonNull
    Optional<Guest> findGuestByPersonPesel(@NonNull String pesel);
    @NonNull
    Optional<Guest> findById(@NonNull Integer id);
    @NonNull
    Guest save(@NonNull Guest guest);
    @NonNull
    boolean existsByPersonPesel(@NonNull String pesel);
}
