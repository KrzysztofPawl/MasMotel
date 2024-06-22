package com.motel.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ReceptionistLanguageId implements Serializable {
    private Integer receptionistId;
    private Integer languageId;

    public ReceptionistLanguageId() {}

    public ReceptionistLanguageId(Receptionist receptionist, Language language) {
        this.receptionistId = receptionist.getId();
        this.languageId = language.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReceptionistLanguageId that = (ReceptionistLanguageId) o;
        return receptionistId.equals(that.receptionistId) && languageId.equals(that.languageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receptionistId, languageId);
    }
}

