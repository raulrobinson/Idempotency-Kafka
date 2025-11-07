package com.example.worker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("transactions")
public class TransactionEntity {
    @Id public UUID id;
    @Column("idem_key") public String idemKey;
    public BigDecimal amount;
    public String payload; // JSON
    public String status;
    public Integer attempts;
    @Column("created_at") public OffsetDateTime createdAt;
    @Column("updated_at") public OffsetDateTime updatedAt;
}