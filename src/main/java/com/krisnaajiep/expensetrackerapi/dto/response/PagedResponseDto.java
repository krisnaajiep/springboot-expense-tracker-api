package com.krisnaajiep.expensetrackerapi.dto.response;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 03/07/25 09.25
@Last Modified 03/07/25 09.25
Version 1.0
*/

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(name = "PagedResponse", description = "Paged response body")
public class PagedResponseDto<T> {
    @Schema(
            description = "List of expenses",
            example = """
                    [
                         {
                           "id": "fa0f58de-6ed9-4502-9b34-016d614aa5ab",
                           "description": "Purchase of new computer",
                           "amount": 800.00,
                           "date": "2025-06-30",
                           "category": "Electronics"
                         },
                         {
                           "id": "8ee69e72-11d2-495c-a9d5-5fe89a095e61",
                           "description": "Electricity and internet bill for the office",
                           "amount": 120.00,
                           "date": "2025-07-15",
                           "category": "Utilities"
                         }
                    ]
                    """
    )
    private List<T> content;

    @Schema(
            description = "Page metadata",
            example = """
                    {
                      "size": 10,
                      "number": 0,
                      "totalElements": 2,
                      "totalPages": 1
                    }
                    """
    )
    private PagedModel.PageMetadata metadata;
}