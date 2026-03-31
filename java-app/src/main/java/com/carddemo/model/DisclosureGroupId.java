package com.carddemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisclosureGroupId implements Serializable {
    private String acctGroupId;
    private String tranTypeCd;
    private Integer tranCatCd;
}
