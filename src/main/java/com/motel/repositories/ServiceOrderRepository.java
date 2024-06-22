package com.motel.repositories;

import com.motel.model.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {

    @NonNull
    List<ServiceOrder> findByReservationId(@NonNull Integer reservationId);
}
