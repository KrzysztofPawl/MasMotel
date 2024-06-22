package com.motel.repositories;

import com.motel.model.Invoice;
import com.motel.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    @NonNull
    Optional<Invoice> findById(@NonNull Integer id);

    @NonNull
    List<Invoice> findByReservationId(@NonNull Integer reservationId);

    @NonNull
    List<Invoice> findByStatus(@NonNull InvoiceStatus status);

    @NonNull
    Invoice save(@NonNull Invoice invoice);
}
