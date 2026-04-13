package com.carddemo.service;

import com.carddemo.model.entity.Transaction;
import org.springframework.stereotype.Service;

/**
 * Centralized service for generating unique transaction IDs and persisting
 * transactions atomically. The generate-and-save must happen inside the same
 * synchronized block so that no other thread can read the same MAX(tran_id)
 * before the new row is committed to the database.
 *
 * The actual persistence is delegated to TransactionIdPersister, which uses
 * {@code @Transactional(propagation = REQUIRES_NEW)} to ensure the save always
 * commits in its own independent transaction. This is critical when called
 * from Spring Batch tasklets, where the tasklet's outer transaction would
 * otherwise keep the save uncommitted until the entire step completes.
 * Without REQUIRES_NEW, the synchronized lock releases before the batch
 * transaction commits, allowing another thread to read MAX(tran_id) and
 * generate a duplicate ID.
 *
 * The delegation to a separate bean is necessary because Spring's CGLIB proxy
 * cannot intercept self-invocation — calling a @Transactional method on
 * the same bean bypasses the proxy entirely.
 */
@Service
public class TransactionIdService {

    private final TransactionIdPersister persister;

    public TransactionIdService(TransactionIdPersister persister) {
        this.persister = persister;
    }

    /**
     * Generate the next transaction ID, assign it to the given Transaction,
     * and save it — all within a single synchronized block. The save happens
     * in a REQUIRES_NEW transaction (via TransactionIdPersister) so the row
     * is committed before the lock releases, even when called from within
     * a Spring Batch step transaction.
     *
     * @param transaction the Transaction entity (all fields set except tranId)
     * @return the saved Transaction with its generated tranId
     */
    public synchronized Transaction generateIdAndSave(Transaction transaction) {
        return persister.computeAndSave(transaction);
    }
}
