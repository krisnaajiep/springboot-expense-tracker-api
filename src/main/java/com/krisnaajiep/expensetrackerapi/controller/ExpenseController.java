package com.krisnaajiep.expensetrackerapi.controller;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 03.04
@Last Modified 30/06/25 03.04
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseFilter;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.mapper.ExpenseMapper;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.security.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping(
            value = "/expenses",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ExpenseResponseDto> save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto
    ) {
        Expense expense = ExpenseMapper.toExpense(userDetails.user(), expenseRequestDto);
        ExpenseResponseDto expenseResponseDto = expenseService.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseResponseDto);
    }

    @PutMapping(
            value = "/expenses/{expenseId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ExpenseResponseDto> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequestDto expenseRequestDto
    ) {
        Expense expense = ExpenseMapper.toExpense(userDetails.user(), expenseRequestDto);
        ExpenseResponseDto expenseResponseDto = expenseService.update(expenseId, expense);
        return ResponseEntity.ok(expenseResponseDto);
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long expenseId
    ) {
        expenseService.delete(userDetails.getId(), expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(
            value = "/expenses",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagedResponseDto<ExpenseResponseDto>> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ExpenseFilter filter,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable
    ) {
        Page<ExpenseResponseDto> expenses = expenseService.findAll(
                userDetails.getId(), filter, from, to, pageable
        );

        return ResponseEntity.ok(new PagedResponseDto<>(
                expenses.getContent(),
                new PagedModel.PageMetadata(
                        expenses.getSize(),
                        expenses.getNumber(),
                        expenses.getTotalElements(),
                        expenses.getTotalPages()
                )
        ));
    }
}
