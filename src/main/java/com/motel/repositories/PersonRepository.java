package com.motel.repositories;

import com.motel.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @NonNull
    Person save(@NonNull Person person);
    @NonNull
    boolean existsByPesel(@NonNull String pesel);
}
