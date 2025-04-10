package com.codeElevate.ExpenseTracker.repository;

import com.codeElevate.ExpenseTracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
}
