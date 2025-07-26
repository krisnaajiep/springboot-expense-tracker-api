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
    private List<T> content;
    private PagedModel.PageMetadata metadata;
}