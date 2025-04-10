package com.codeElevate.ExpenseTracker.controller;

import com.codeElevate.ExpenseTracker.dto.ExpenseDto;
import com.codeElevate.ExpenseTracker.entity.Expense;
import com.codeElevate.ExpenseTracker.services.expense.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

   /* @GetMapping("/expenses/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        List<Expense> expenses = expenseService.getAllExpenses();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");
            CreationHelper helper = workbook.getCreationHelper();

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(helper.createDataFormat().getFormat("₹#,##0.00"));

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy"));

            // Header row
            String[] columns = {"ID", "Title", "Description", "Amount", "Date", "Category"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            double totalAmount = 0.0;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getId());
                row.createCell(1).setCellValue(expense.getTitle());
                row.createCell(2).setCellValue(expense.getDescription());

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(expense.getAmount().doubleValue());
                amountCell.setCellStyle(currencyStyle);
                totalAmount += expense.getAmount().doubleValue();

                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(java.sql.Date.valueOf(expense.getDate())); // or convert if it's not LocalDate
                dateCell.setCellStyle(dateStyle);

                row.createCell(5).setCellValue(expense.getCategory());
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("Total:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalAmountCell = totalRow.createCell(3);
            totalAmountCell.setCellValue(totalAmount);
            totalAmountCell.setCellStyle(currencyStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Optional: Protect sheet
            // sheet.protectSheet("yourPassword");
            workbook.write(response.getOutputStream());
        }
    }*/

    @GetMapping("/expenses/excel")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        //List<Expense> expenses = expenseRepository.findAll();
        List<Expense> expenses = expenseService.getAllExpenses();

        // Group by Date (sorted)
        Map<LocalDate, List<Expense>> groupedByDate = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getDate, TreeMap::new, Collectors.toList()));

        // Set response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");

            int rowNum = 0;
            double grandTotal = 0.0;

            // Style for bold rows
            CellStyle boldStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            boldStyle.setFont(font);

            for (Map.Entry<LocalDate, List<Expense>> entry : groupedByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Expense> dailyExpenses = entry.getValue();

                // Header row for the date
                Row dateHeader = sheet.createRow(rowNum++);
                Cell dateCell = dateHeader.createCell(0);
                dateCell.setCellValue("Date: " + date.toString());
                dateCell.setCellStyle(boldStyle);

                // Column headers
                Row header = sheet.createRow(rowNum++);
                header.createCell(0).setCellValue("ID");
                header.createCell(1).setCellValue("Title");
                header.createCell(2).setCellValue("Description");
                header.createCell(3).setCellValue("Amount");
                header.createCell(4).setCellValue("Category");

                double dailyTotal = 0.0;

                // Add rows for each expense
                for (Expense e : dailyExpenses) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(e.getId());
                    row.createCell(1).setCellValue(e.getTitle());
                    row.createCell(2).setCellValue(e.getDescription());
                    row.createCell(3).setCellValue(e.getAmount());
                    row.createCell(4).setCellValue(e.getCategory());

                    dailyTotal += e.getAmount();
                }

                // Row for daily total
                Row totalRow = sheet.createRow(rowNum++);
                Cell totalLabel = totalRow.createCell(2);
                totalLabel.setCellValue("Total for " + date.toString());
                totalLabel.setCellStyle(boldStyle);
                Cell totalAmount = totalRow.createCell(3);
                totalAmount.setCellValue(dailyTotal);
                totalAmount.setCellStyle(boldStyle);

                // Empty row between groups
                rowNum++;

                grandTotal += dailyTotal;
            }

            // Final cumulative total row
            Row grandTotalRow = sheet.createRow(rowNum++);
            Cell grandLabel = grandTotalRow.createCell(2);
            grandLabel.setCellValue("Cumulative Total");
            grandLabel.setCellStyle(boldStyle);
            Cell grandAmount = grandTotalRow.createCell(3);
            grandAmount.setCellValue(grandTotal);
            grandAmount.setCellStyle(boldStyle);

            // Auto-size columns
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
       // expenseRepository.save(expense);

        Expense createdExpense = expenseService.postExpense(expenseDto);

        redirectAttributes.addFlashAttribute("message", "Expense saved successfully!");
        return "redirect:/api/expense/expenses/new";
    }

}
