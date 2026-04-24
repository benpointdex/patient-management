package com.pm.billing_serivce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceRequest {
    private String patientId;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String serviceName; // Switch to Name
        private BigDecimal price;    // Switch to Price
        private Integer quantity;
        private BigDecimal amount;
    }
}
