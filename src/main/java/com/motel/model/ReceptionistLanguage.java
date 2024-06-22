package com.motel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "receptionist_language")
public class ReceptionistLanguage {
    @EmbeddedId
    private ReceptionistLanguageId id;

    @ManyToOne
    @MapsId("receptionistId")
    @JoinColumn(name = "receptionist_id")
    private Receptionist receptionist;

    @ManyToOne
    @MapsId("languageId")
    @JoinColumn(name = "language_id")
    private Language language;

    public ReceptionistLanguage() {
    }

    public ReceptionistLanguage(Receptionist receptionist, Language language) {
        this.receptionist = receptionist;
        this.language = language;
        this.id = new ReceptionistLanguageId(receptionist, language);
    }
}