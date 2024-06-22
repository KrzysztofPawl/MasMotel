package com.motel.interfaces.service;

import com.motel.model.Person;

import java.util.Optional;

public interface PersonService {
    Person savePerson(Person person);
    Person getPersonById(int id);
    boolean existsByPesel(String pesel);
    Optional<Person> getPersonByPeselNoException(String pesel);

}
