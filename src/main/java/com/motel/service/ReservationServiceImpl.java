package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.InvoiceService;
import com.motel.interfaces.service.RoomService;
import com.motel.model.Invoice;
import com.motel.model.Room;
import com.motel.model.enums.RoomStatus;
import com.motel.repositories.InvoiceRepository;
import com.motel.repositories.ReservationRepository;
import com.motel.interfaces.service.ReservationService;
import com.motel.model.Reservation;
import com.motel.model.enums.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final RoomService roomService;

    @Override
    @Transactional
    public Reservation saveReservation(Reservation reservation) {
        var result = reservationRepository.save(reservation);
        log.info("Reservation saved: {}", result);
        return result;
    }

    @Override
    public Reservation getReservationById(int id) {
        var result = reservationRepository.findById(id).orElse(null);
        if (result == null) {
            log.warn("Reservation not found: {}", id);
            throw new DataNotFoundException("Reservation not found: " + id);
        }
        log.info("Reservation found: {}", result);
        return result;
    }

    @Override
    public Reservation getReservationByIdNotDeleted(int id) {
        var result = reservationRepository.findById(id).orElse(null);
        if (result == null || result.isDeleted()) {
            log.warn("Reservation not found: {}", id);
            throw new DataNotFoundException("Reservation not found: " + id);
        }
        log.info("Reservation found: {}", result);
        return result;
    }

    @Override
    public List<Reservation> getGuestReservationsNotDeleted(String pesel) {
        var result = reservationRepository.findReservationsByPesel(pesel);
        if (result.isEmpty()) {
            log.warn("Reservations not found for guest: {}", pesel);
            throw new DataNotFoundException("Reservations not found for guest: " + pesel);
        }
        result = result.stream().filter(reservation -> !reservation.isDeleted()).toList();

        log.info("Reservations found: {}", result);
        return result;
    }

    @Override
    @Transactional
    public Reservation changeReservationStatus(int id, ReservationStatus status) {
        var reservation = getReservationById(id);
        reservation.setStatus(status);
        var result = reservationRepository.save(reservation);
        log.info("Reservation status changed: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void markReservationAsDeletedAndDeleteInvoices(int id) {
        var reservation = getReservationById(id);
        if (reservation.isDeleted()) {
            log.warn("Reservation already deleted: {}", id);
            throw new IllegalStateException("Reservation already deleted: " + id);
        }
        reservation.markAsDeleted();

        List<Invoice> invoices = reservation.getInvoices().stream().toList();
        for (Invoice invoice : invoices) {
            reservation.getInvoices().remove(invoice);
            invoiceRepository.delete(invoice);
            log.info("Invoice deleted: {}", invoice.getId());
        }

        invoiceRepository.deleteAll(invoices);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void clearRoomAssignmentForReservation(int id) {
        var reservation = getReservationById(id);
        if (reservation.getStatus() != ReservationStatus.OPEN) {
            for (Room room : reservation.getRooms()) {
                room.setStatus(RoomStatus.AVAILABLE);
                room.setReservation(null);
                roomService.saveRoom(room);
                log.info("Room assignment cleared: {}", room.getId());
            }
        } else {
            log.warn("Reservation is OPEN, cannot clear room assignment: {}", id);
            throw new IllegalStateException("Reservation is OPEN, cannot clear room assignment: " + id);
        }

    }

    @Override
    public boolean existsById(int id) {
        var result = reservationRepository.existsById(id);
        log.info("Reservation exists: {}", result);
        return result;
    }
}
