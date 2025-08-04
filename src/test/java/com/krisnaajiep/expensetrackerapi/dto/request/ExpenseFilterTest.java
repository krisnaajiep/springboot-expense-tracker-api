package com.krisnaajiep.expensetrackerapi.dto.request;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseFilterTest {

    @Test
    void testPastWeek() {
        ExpenseFilter filter = ExpenseFilter.PAST_WEEK;
        ExpenseFilter.DateRange actual = filter.resolve();
        ExpenseFilter.DateRange expected = new ExpenseFilter.DateRange(
                LocalDate.now().minusDays(7), LocalDate.now()
        );

        assertEquals(expected, actual);
    }

    @Test
    void testPastMonth() {
        ExpenseFilter filter = ExpenseFilter.PAST_MONTH;
        ExpenseFilter.DateRange actual = filter.resolve();
        ExpenseFilter.DateRange expected = new ExpenseFilter.DateRange(
                LocalDate.now().minusMonths(1), LocalDate.now()
        );

        assertEquals(expected, actual);
    }

    @Test
    void testLast3Months() {
        ExpenseFilter filter = ExpenseFilter.LAST_3_MONTHS;
        ExpenseFilter.DateRange actual = filter.resolve();
        ExpenseFilter.DateRange expected = new ExpenseFilter.DateRange(
                LocalDate.now().minusMonths(3), LocalDate.now()
        );

        assertEquals(expected, actual);
    }
}