package com.motel.repositories;

import com.motel.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @NonNull
    Optional<Employee> findById(@NonNull Integer id);

    @NonNull
    List<Employee> findAll();

    @NonNull
    Employee save(@NonNull Employee employee);
}