package com.carddemo.reference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CardDemo Reference Data service.
 *
 * <p>This Spring Boot application is the Phase 2 modernization of the legacy
 * DB2 "Transaction Type Management" optional module of CardDemo. It replaces
 * the following COBOL programs (under
 * {@code app/app-transaction-type-db2/cbl/}):</p>
 * <ul>
 *   <li>{@code COTRTLIC.cbl} &ndash; CICS online list/update/delete of
 *       transaction types (DB2 cursor processing).</li>
 *   <li>{@code COTRTUPC.cbl} &ndash; CICS online add/edit of transaction
 *       types (DB2 INSERT/UPDATE).</li>
 *   <li>{@code COBTUPDT.cbl} &ndash; batch transaction type maintenance
 *       (DB2 INSERT/UPDATE/DELETE driven by a control file).</li>
 * </ul>
 *
 * <p>The relational model mirrors the original DB2 DDL in
 * {@code app/app-transaction-type-db2/ddl/} (TRNTYPE.ddl and TRNTYCAT.ddl)
 * and the VSAM copybooks {@code app/cpy/CVTRA03Y.cpy} (transaction type) and
 * {@code app/cpy/CVTRA04Y.cpy} (transaction category).</p>
 */
@SpringBootApplication
public class ReferenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferenceApplication.class, args);
    }
}
