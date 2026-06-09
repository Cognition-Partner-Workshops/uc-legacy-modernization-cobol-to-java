package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.CardXref;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CardXrefRepository cardXrefRepository;

    public CardService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    @Transactional(readOnly = true)
    public Page<CardXref> findAll(Pageable pageable) {
        return cardXrefRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public CardXref findByCardNum(String cardNum) {
        return cardXrefRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardNum));
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByCustomerId(long custId) {
        return cardXrefRepository.findByCustId(custId);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByAccountId(long acctId) {
        return cardXrefRepository.findByAcctId(acctId);
    }

    @Transactional
    public CardXref create(CardXref card) {
        return cardXrefRepository.save(card);
    }

    @Transactional
    public CardXref update(String cardNum, CardXref updated) {
        CardXref existing = findByCardNum(cardNum);
        existing.setCustId(updated.getCustId());
        existing.setAcctId(updated.getAcctId());
        return cardXrefRepository.save(existing);
    }
}
