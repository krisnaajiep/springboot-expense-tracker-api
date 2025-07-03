package com.krisnaajiep.expensetrackerapi.repository;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.14
@Last Modified 30/06/25 02.14
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Additional query methods can be defined here if needed
    @Query("""
            SELECT e FROM Expense e
                        WHERE e.user.id = :userId
                        AND (:startDate IS NULL OR e.date >= :startDate)
                        AND (:endDate IS NULL OR e.date <= :endDate)
            """)
    Page<Expense> findAll(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
