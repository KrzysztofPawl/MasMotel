package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.InvoiceService;
import com.motel.model.Invoice;
import com.motel.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
}
