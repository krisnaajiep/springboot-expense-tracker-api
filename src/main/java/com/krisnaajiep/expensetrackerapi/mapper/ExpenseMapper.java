package com.krisnaajiep.expensetrackerapi.mapper;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 07.53
@Last Modified 30/06/25 07.53
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.web.util.HtmlUtils;

public class ExpenseMapper {
    public static Expense toExpense(User user, ExpenseRequestDto expenseRequestDto) {
        return Expense.builder()
                .user(user)
                .description(HtmlUtils.htmlEscape(expenseRequestDto.getDescription().trim(), "UTF-8"))
                .amount(expenseRequestDto.getAmount())
                .category(Expense.Category.fromDisplayName(expenseRequestDto.getCategory()))
                .date(expenseRequestDto.getDate())
                .build();
    }

    public static ExpenseResponseDto toExpenseResponseDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory().getDisplayName(),
                expense.getDate()
        );
    }

    public static PagedResponseDto<ExpenseResponseDto> toPagedResponseDto(Page<ExpenseResponseDto> page) {
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new PagedResponseDto<>(page.getContent(), metadata);
    }
}
