package com.codeElevate.ExpenseTracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String description;

    private String category;

    private LocalDate date;

    private Integer amount;

    public Expense(String description, String category, LocalDate date, Integer amount) {
        this.description = description;
        this.category = category;
        this.date = date;
        this.amount = amount;
    }
}
