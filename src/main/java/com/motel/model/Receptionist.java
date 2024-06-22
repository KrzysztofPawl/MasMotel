package com.motel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "receptionist")
@PrimaryKeyJoinColumn(name = "employee_id")
public class Receptionist extends Employee {

    @OneToMany(mappedBy = "receptionist")
    private Set<ReceptionistLanguage> languages;

    public void addLanguage(Language language) {
        ReceptionistLanguage receptionistLanguage = new ReceptionistLanguage(this, language);
        languages.add(receptionistLanguage);
        language.getReceptionists().add(receptionistLanguage);
    }
}
