package com.krisnaajiep.expensetrackerapi.dto.request;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 02/07/25 15.56
@Last Modified 02/07/25 15.56
Version 1.0
*/

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public enum ExpenseFilter {
    PAST_WEEK, PAST_MONTH, LAST_3_MONTHS;

    public DateRange resolve() {
        LocalDate now = LocalDate.now();
        return switch (this) {
            case PAST_WEEK -> new DateRange(now.minusDays(7), now);
            case PAST_MONTH -> new DateRange(now.minusMonths(1), now);
            case LAST_3_MONTHS -> new DateRange(now.minusMonths(3), now);
        };
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
