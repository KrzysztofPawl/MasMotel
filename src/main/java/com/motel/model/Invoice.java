package com.motel.model;

import com.motel.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "date_of_issue")
    private LocalDate dateOfIssue;

    @Column(name = "sum")
    private BigDecimal sum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @Column(name = "payment_date", nullable = true)
    private LocalDate paymentDate;

    @Column(name = "payment_method", nullable = true)
    private String paymentMethod;

    @Column(name = "vat")
    private Integer vat;

    public void markAsPaid(String paymentMethod) {
        this.status = InvoiceStatus.PAID;
        this.paymentDate = LocalDate.now();
        this.paymentMethod = paymentMethod;
    }

}
