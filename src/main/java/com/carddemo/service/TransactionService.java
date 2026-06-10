package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Transaction;
import com.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Transaction findById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findByCardNum(String cardNum, Pageable pageable) {
        return transactionRepository.findByCardNumOrderByOriginTimestampDesc(cardNum, pageable);
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        if (transaction.getTranId() != null && transactionRepository.existsById(transaction.getTranId())) {
            throw new IllegalArgumentException("Transaction already exists with id: " + transaction.getTranId());
        }
        return transactionRepository.save(transaction);
    }
}
