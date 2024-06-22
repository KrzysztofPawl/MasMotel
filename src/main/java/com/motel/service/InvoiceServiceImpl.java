package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.InvoiceService;
import com.motel.model.Invoice;
import com.motel.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoiceById(int id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Invoice not found: " + id));
    }

    @Override
    @Transactional
    public void deleteAllByReservationId(int reservationId) {
        List<Invoice> invoices = invoiceRepository.findByReservationId(reservationId);
        invoiceRepository.deleteAll(invoices);
        log.info("Invoices deleted: {}", invoices);
    }
}
