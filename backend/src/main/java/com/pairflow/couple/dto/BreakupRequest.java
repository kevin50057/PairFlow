package com.pairflow.couple.dto;

import com.pairflow.couple.DataHandling;

public record BreakupRequest(Boolean confirm, DataHandling dataHandling) {
}
