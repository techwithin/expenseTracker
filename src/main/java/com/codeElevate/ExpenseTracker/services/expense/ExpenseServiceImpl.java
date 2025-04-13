package com.codeElevate.ExpenseTracker.services.expense;

import com.codeElevate.ExpenseTracker.dto.ExpenseDto;
import com.codeElevate.ExpenseTracker.entity.Expense;
import com.codeElevate.ExpenseTracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Expense postExpense(ExpenseDto dto) {
        return saveOrUpdateExpense(new Expense(), dto);
    }

    private Expense saveOrUpdateExpense(Expense expense, ExpenseDto expenseDto) {
        expense.setDate(expenseDto.getDate());
        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setCategory(expenseDto.getCategory());
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(){
        return expenseRepository.findAll().stream().sorted((a,b)->a.getAmount()-b.getAmount()).collect(Collectors.toList());

    }
}
