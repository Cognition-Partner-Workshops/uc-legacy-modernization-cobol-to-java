package com.carddemo.refdata.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.carddemo.refdata.entity.DisclosureGroup;
import com.carddemo.refdata.entity.DisclosureGroupId;
import com.carddemo.refdata.repository.DisclosureGroupRepository;
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
class DisclosureGroupServiceTest {

    @Mock
    private DisclosureGroupRepository repository;

    @InjectMocks
    private DisclosureGroupService service;

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(
                new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("12.50"))));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByKey_found() {
        DisclosureGroupId id = new DisclosureGroupId("GRP001", "SA", 1001);
        when(repository.findById(id)).thenReturn(
                Optional.of(new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("12.50"))));
        DisclosureGroup result = service.findByKey("GRP001", "SA", 1001);
        assertNotNull(result);
        assertEquals(new BigDecimal("12.50"), result.getInterestRate());
    }

    @Test
    void findByKey_notFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertNull(service.findByKey("X", "ZZ", 0));
    }

    @Test
    void create_success() {
        DisclosureGroup dg = new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("12.50"));
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(dg)).thenReturn(dg);
        DisclosureGroup result = service.create(dg);
        assertEquals("GRP001", result.getAccountGroupId());
    }

    @Test
    void create_duplicate_throws() {
        DisclosureGroup dg = new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("12.50"));
        when(repository.existsById(any())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(dg));
        verify(repository, never()).save(any());
    }

    @Test
    void update_found() {
        DisclosureGroup existing = new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("12.50"));
        when(repository.findById(any())).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DisclosureGroup update = new DisclosureGroup("GRP001", "SA", 1001, new BigDecimal("18.75"));
        DisclosureGroup result = service.update("GRP001", "SA", 1001, update);
        assertNotNull(result);
        assertEquals(new BigDecimal("18.75"), result.getInterestRate());
    }

    @Test
    void update_notFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertNull(service.update("X", "ZZ", 0,
                new DisclosureGroup("X", "ZZ", 0, BigDecimal.ONE)));
    }

    @Test
    void delete_found() {
        when(repository.existsById(any())).thenReturn(true);
        assertTrue(service.delete("GRP001", "SA", 1001));
    }

    @Test
    void delete_notFound() {
        when(repository.existsById(any())).thenReturn(false);
        assertFalse(service.delete("X", "ZZ", 0));
    }
}
