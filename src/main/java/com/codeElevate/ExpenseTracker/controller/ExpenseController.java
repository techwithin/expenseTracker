package com.codeElevate.ExpenseTracker.controller;

import com.codeElevate.ExpenseTracker.dto.ExpenseDto;
import com.codeElevate.ExpenseTracker.dto.ExpensesWrapper;
import com.codeElevate.ExpenseTracker.entity.Expense;
import com.codeElevate.ExpenseTracker.repository.ExpenseRepository;
import com.codeElevate.ExpenseTracker.services.expense.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/expense")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> postExpense(@RequestBody ExpenseDto dto){
        Expense createdExpense = expenseService.postExpense(dto);
        if (createdExpense != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<?> getAllExpenses(){
        return  ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/expenses/excel")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        List<Expense> expenses = expenseService.getAllExpenses();

        Map<LocalDate, List<Expense>> groupedByDate = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getDate, TreeMap::new, Collectors.toList()));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");
            int rowNum = 0;
            double grandTotal = 0.0;

            // Style definitions (same as before)
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(boldStyle);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle borderStyle = workbook.createCellStyle();
            borderStyle.setBorderTop(BorderStyle.THIN);
            borderStyle.setBorderBottom(BorderStyle.THIN);
            borderStyle.setBorderLeft(BorderStyle.THIN);
            borderStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dateHeaderStyle = workbook.createCellStyle();
            dateHeaderStyle.cloneStyleFrom(boldStyle);
            dateHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            dateHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(boldStyle);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setBorderTop(BorderStyle.THIN);
            totalStyle.setBorderBottom(BorderStyle.THIN);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);

            for (Map.Entry<LocalDate, List<Expense>> entry : groupedByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Expense> dailyExpenses = entry.getValue();

                // Date Header
                Row dateRow = sheet.createRow(rowNum++);
                Cell dateCell = dateRow.createCell(0);
                dateCell.setCellValue("Date: " + date.toString());
                dateCell.setCellStyle(dateHeaderStyle);

                // Column Headers (updated: removed Title)
                Row headerRow = sheet.createRow(rowNum++);
                String[] columns = {"ID", "Description", "Amount", "Category"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                double dailyTotal = 0.0;

                for (Expense e : dailyExpenses) {
                    Row row = sheet.createRow(rowNum++);

                    Cell idCell = row.createCell(0);
                    idCell.setCellValue(e.getId());
                    idCell.setCellStyle(borderStyle);

                    Cell descCell = row.createCell(1);
                    descCell.setCellValue(e.getDescription());
                    descCell.setCellStyle(borderStyle);

                    Cell amountCell = row.createCell(2);
                    amountCell.setCellValue(e.getAmount());
                    amountCell.setCellStyle(borderStyle);

                    Cell catCell = row.createCell(3);
                    catCell.setCellValue(e.getCategory());
                    catCell.setCellStyle(borderStyle);

                    dailyTotal += e.getAmount();
                }

                // Daily total
                Row totalRow = sheet.createRow(rowNum++);
                Cell labelCell = totalRow.createCell(2);
                labelCell.setCellValue("Total for " + date);
                labelCell.setCellStyle(totalStyle);

                Cell totalCell = totalRow.createCell(3);
                totalCell.setCellValue(dailyTotal);
                totalCell.setCellStyle(totalStyle);

                rowNum++; // Spacer
                grandTotal += dailyTotal;
            }

            // Cumulative Total
            Row grandTotalRow = sheet.createRow(rowNum++);
            Cell grandLabel = grandTotalRow.createCell(2);
            grandLabel.setCellValue("Cumulative Total");
            grandLabel.setCellStyle(totalStyle);

            Cell grandAmount = grandTotalRow.createCell(3);
            grandAmount.setCellValue(grandTotal);
            grandAmount.setCellStyle(totalStyle);

            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }




    @GetMapping("/expenses/new")
    public String showExpenseForm(Model model) {
        model.addAttribute("expenseDto", new ExpenseDto());
        return "expense";
    }

    @PostMapping("/addExpenses")
    public String saveExpense(@ModelAttribute ExpenseDto expenseDto, RedirectAttributes redirectAttributes) {
        Expense createdExpense = expenseService.postExpense(expenseDto);
        redirectAttributes.addFlashAttribute("message", "Expense saved successfully!");
        return "redirect:/api/expense/expenses/new";
    }

    @PostMapping("/expenses/bulk")
    public String saveExpenses(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               @ModelAttribute ExpensesWrapper expensesWrapper,
                               RedirectAttributes redirectAttributes) {
        try {
            List<Expense> expenseList = expensesWrapper.getExpenses().stream()
                    .map(dto -> new Expense(dto.getDescription(), dto.getCategory(), date, dto.getAmount()))
                    .collect(Collectors.toList());

            expenseRepository.saveAll(expenseList);
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Expenses saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("success", false);
            redirectAttributes.addFlashAttribute("message", "Failed to save expenses.");
        }
        return "redirect:/api/expense/expenses/form";
    }



    @GetMapping("/expenses/form")
    public String showBulkForm(Model model) {

        ExpensesWrapper wrapper = new ExpensesWrapper();
        for (int i = 0; i < 5; i++) {
            wrapper.getExpenses().add(new ExpenseDto());
        }
        model.addAttribute("expensesWrapper", wrapper);
        return "bulk-expense-form";
    }

}
