package com.codeElevate.ExpenseTracker.services.expense;

import com.codeElevate.ExpenseTracker.dto.ExpenseDto;
import com.codeElevate.ExpenseTracker.entity.Expense;
import com.codeElevate.ExpenseTracker.repository.ExpenseRepository;

import java.util.List;

public interface ExpenseService {
    Expense postExpense(ExpenseDto dto);
    public List<Expense> getAllExpenses();

}
