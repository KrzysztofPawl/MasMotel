package com.motel.DTO;

import com.motel.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ReservationView {
    private Integer Reservation_id;
    private String Room_Numbers;
    private LocalDate Reservation_Date;
    private LocalDate Arrival_Date;
    private LocalDate Leave_Date;
    private Integer Nights;
    private ReservationStatus Status;
}