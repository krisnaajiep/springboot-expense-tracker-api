package com.krisnaajiep.expensetrackerapi.model;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 01.46
@Last Modified 30/06/25 01.46
Version 1.0
*/

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(callSuper = true)
public class Expense extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Description", nullable = false)
    private String description;

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "Category", nullable = false, length = 20)
    private Category category;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Getter
    @RequiredArgsConstructor
    public enum Category {
        GROCERIES ("Groceries"),
        LEISURE ("Leisure"),
        ELECTRONICS ("Electronics"),
        UTILITIES ("Utilities"),
        CLOTHING ("Clothing"),
        HEALTH ("Health"),
        OTHERS ("Others");

        private final String displayName;

        public static Category fromDisplayName(String displayName) {
            for (Category category : Category.values()) {
                if (category.getDisplayName().equals(displayName)) {
                    return category;
                }
            }

            throw new IllegalArgumentException("No category found for display name: " + displayName);
        }
    }
}

