package com.motel.interfaces.service;

import com.motel.model.Invoice;

public interface InvoiceService {
    Invoice saveInvoice(Invoice invoice);
    Invoice getInvoiceById(int id);

    void deleteAllByReservationId(int reservationId);
}
