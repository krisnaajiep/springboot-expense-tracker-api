package com.krisnaajiep.expensetrackerapi.repository;

import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
}
