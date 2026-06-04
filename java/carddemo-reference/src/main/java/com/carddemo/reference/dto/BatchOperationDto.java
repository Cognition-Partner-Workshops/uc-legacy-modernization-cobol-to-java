package com.carddemo.reference.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A single transaction-type maintenance operation for the batch endpoint.
 *
 * <p>Replaces the control-file-driven {@code COBTUPDT} batch program, which
 * read a record describing an action and applied an INSERT / UPDATE / DELETE
 * to the DB2 transaction type table. The whole batch is processed in one
 * transaction so a single failure rolls back the entire run, matching the
 * all-or-nothing intent of the original batch job.</p>
 */
public class BatchOperationDto {

    /** Supported maintenance actions, mirroring COBTUPDT. */
    public enum Action {
        INSERT,
        UPDATE,
        DELETE
    }

    @NotNull(message = "action is required")
    private Action action;

    @NotNull(message = "typeCode is required")
    @Size(min = 2, max = 2, message = "Transaction type code must be exactly 2 characters")
    private String typeCode;

    /** Required for INSERT/UPDATE; ignored for DELETE. Max 50 chars. */
    @Size(max = 50, message = "Description must be at most 50 characters")
    private String description;

    public BatchOperationDto() {
    }

    public BatchOperationDto(Action action, String typeCode, String description) {
        this.action = action;
        this.typeCode = typeCode;
        this.description = description;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
