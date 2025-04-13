package com.codeElevate.ExpenseTracker.dto;

import com.codeElevate.ExpenseTracker.dto.ExpenseDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ExpensesWrapper {
    private List<ExpenseDto> expenses = new ArrayList<>();

}
