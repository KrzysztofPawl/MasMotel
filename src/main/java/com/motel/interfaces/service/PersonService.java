package com.motel.interfaces.service;

import com.motel.model.Person;

public interface PersonService {
    Person savePerson(Person person);
    Person getPersonById(int id);
    boolean existsByPesel(String pesel);

}
