package com.carddemo.refdata.service;

import java.util.List;
import java.util.Optional;

import com.carddemo.refdata.entity.TransactionCategory;
import com.carddemo.refdata.entity.TransactionCategoryId;
import com.carddemo.refdata.repository.TransactionCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTest {

    @Mock
    private TransactionCategoryRepository repository;

    @InjectMocks
    private TransactionCategoryService service;

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(
                new TransactionCategory("SA", 1001, "Online Sale")));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByKey_found() {
        TransactionCategoryId id = new TransactionCategoryId("SA", 1001);
        when(repository.findById(id)).thenReturn(
                Optional.of(new TransactionCategory("SA", 1001, "Online Sale")));
        TransactionCategory result = service.findByKey("SA", 1001);
        assertNotNull(result);
        assertEquals("Online Sale", result.getDescription());
    }

    @Test
    void findByKey_notFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertNull(service.findByKey("ZZ", 9999));
    }

    @Test
    void create_success() {
        TransactionCategory cat = new TransactionCategory("SA", 1001, "Online Sale");
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(cat)).thenReturn(cat);
        TransactionCategory result = service.create(cat);
        assertEquals(1001, result.getCategoryCode());
    }

    @Test
    void create_duplicate_throws() {
        TransactionCategory cat = new TransactionCategory("SA", 1001, "Online Sale");
        when(repository.existsById(any())).thenReturn(true);
        assertThrows(DuplicateEntityException.class, () -> service.create(cat));
        verify(repository, never()).save(any());
    }

    @Test
    void update_found() {
        TransactionCategory existing = new TransactionCategory("SA", 1001, "Online Sale");
        when(repository.findById(any())).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TransactionCategory update = new TransactionCategory("SA", 1001, "Updated");
        TransactionCategory result = service.update("SA", 1001, update);
        assertNotNull(result);
        assertEquals("Updated", result.getDescription());
    }

    @Test
    void update_notFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertNull(service.update("ZZ", 9999, new TransactionCategory("ZZ", 9999, "Nope")));
    }

    @Test
    void delete_found() {
        when(repository.existsById(any())).thenReturn(true);
        assertTrue(service.delete("SA", 1001));
    }

    @Test
    void delete_notFound() {
        when(repository.existsById(any())).thenReturn(false);
        assertFalse(service.delete("ZZ", 9999));
    }
}
