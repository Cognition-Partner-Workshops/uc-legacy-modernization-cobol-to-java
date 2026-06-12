package com.carddemo.refdata.service;

import java.util.List;
import java.util.Optional;

import com.carddemo.refdata.entity.TransactionType;
import com.carddemo.refdata.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionTypeServiceTest {

    @Mock
    private TransactionTypeRepository repository;

    @InjectMocks
    private TransactionTypeService service;

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(
                new TransactionType("SA", "Sale"),
                new TransactionType("RE", "Return")));
        List<TransactionType> result = service.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void findByCode_found() {
        when(repository.findById("SA")).thenReturn(Optional.of(new TransactionType("SA", "Sale")));
        TransactionType result = service.findByCode("SA");
        assertNotNull(result);
        assertEquals("Sale", result.getDescription());
    }

    @Test
    void findByCode_notFound() {
        when(repository.findById("ZZ")).thenReturn(Optional.empty());
        assertNull(service.findByCode("ZZ"));
    }

    @Test
    void create_success() {
        TransactionType tt = new TransactionType("SA", "Sale");
        when(repository.existsById("SA")).thenReturn(false);
        when(repository.save(tt)).thenReturn(tt);
        TransactionType result = service.create(tt);
        assertEquals("SA", result.getTypeCode());
    }

    @Test
    void create_duplicate_throws() {
        TransactionType tt = new TransactionType("SA", "Sale");
        when(repository.existsById("SA")).thenReturn(true);
        assertThrows(DuplicateEntityException.class, () -> service.create(tt));
        verify(repository, never()).save(any());
    }

    @Test
    void update_found() {
        TransactionType existing = new TransactionType("SA", "Sale");
        when(repository.findById("SA")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TransactionType update = new TransactionType("SA", "Updated");
        TransactionType result = service.update("SA", update);
        assertNotNull(result);
        assertEquals("Updated", result.getDescription());
    }

    @Test
    void update_notFound() {
        when(repository.findById("ZZ")).thenReturn(Optional.empty());
        assertNull(service.update("ZZ", new TransactionType("ZZ", "Nope")));
    }

    @Test
    void delete_found() {
        when(repository.existsById("SA")).thenReturn(true);
        assertTrue(service.delete("SA"));
        verify(repository).deleteById("SA");
    }

    @Test
    void delete_notFound() {
        when(repository.existsById("ZZ")).thenReturn(false);
        assertFalse(service.delete("ZZ"));
    }
}
