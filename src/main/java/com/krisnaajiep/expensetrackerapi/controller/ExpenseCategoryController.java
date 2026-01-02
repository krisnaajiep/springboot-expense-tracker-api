package com.krisnaajiep.expensetrackerapi.controller;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.15
@Last Modified 30/06/25 02.15
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseCategoryResponseDto;
import com.krisnaajiep.expensetrackerapi.service.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Expense Category", description = "Expense category endpoints")
@RestController
@RequiredArgsConstructor
public class ExpenseCategoryController {
    private final ExpenseCategoryService expenseCategoryService;

    @Operation(summary = "List all expense categories")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense categories listed successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ExpenseCategoryResponseDto.class)),
                            examples = @ExampleObject(
                                    value = """
                                            [
                                                {"id": 1, "name": "Others"},
                                                {"id": 2, "name": "Health"},
                                                {"id": 3, "name": "Clothing"},
                                                {"id": 4, "name": "Utilities"},
                                                {"id": 5, "name": "Electronics"},
                                                {"id": 6, "name": "Leisure"},
                                                {"id": 7, "name": "Groceries"}
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @GetMapping(
            value = "/categories",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ExpenseCategoryResponseDto>> findAll() {
        List<ExpenseCategoryResponseDto> categories = expenseCategoryService.findAll();
        return ResponseEntity.ok(categories);
    }
}
