package com.motel.repositories;

import com.motel.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    @NonNull
    @Transactional
    List<Reservation> findReservationsByPesel(@NonNull String pesel);
    @NonNull
    Optional<Reservation> findById(@NonNull Integer id);

    @NonNull
    Reservation save(@NonNull Reservation reservation);
    void deleteById(@NonNull Integer id);

    @NonNull
    boolean existsById(@NonNull Integer id);

}
