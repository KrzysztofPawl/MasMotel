package com.motel.service;

import com.motel.exception.DataNotFoundException;
import com.motel.interfaces.service.PersonService;
import com.motel.model.Person;
import com.motel.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public Person savePerson(Person person) {
        log.info("Person saved: {}", person);
        return personRepository.save(person);
    }

    @Override
    public Person getPersonById(int id) {
        var result = personRepository.findById(id).orElse(null);

        if (result == null) {
            log.warn("Person not found: {}", id);
            throw new DataNotFoundException("Person not found: " + id);
        }
        log.info("Person found: {}", result);
        return result;
    }

    @Override
    public boolean existsByPesel(String pesel) {
        log.info("Checking if person exists by pesel: {}", pesel);
        return personRepository.existsByPesel(pesel);
    }

    @Override
    public Optional<Person> getPersonByPeselNoException(String pesel) {
        log.info("Fetching person by pesel: {}", pesel);
        return Optional.of(personRepository.findPersonByPesel(pesel));
    }
}
