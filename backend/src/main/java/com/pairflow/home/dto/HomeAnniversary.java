package com.pairflow.home.dto;

import java.time.LocalDate;

public record HomeAnniversary(String title, long daysLeft, LocalDate date) {
}
