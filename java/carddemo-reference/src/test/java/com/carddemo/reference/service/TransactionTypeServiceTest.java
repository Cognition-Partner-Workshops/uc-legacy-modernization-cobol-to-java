package com.carddemo.reference.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.reference.dto.BatchOperationDto;
import com.carddemo.reference.dto.TransactionCategoryDto;
import com.carddemo.reference.dto.TransactionTypeDto;
import com.carddemo.reference.exception.DuplicateResourceException;
import com.carddemo.reference.exception.ReferentialIntegrityException;
import com.carddemo.reference.exception.ResourceNotFoundException;
import com.carddemo.reference.repository.TransactionTypeRepository;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

/**
 * Service-layer tests for {@link TransactionTypeService}, validating the
 * business rules migrated from {@code COTRTLIC}, {@code COTRTUPC} and the
 * transactional batch program {@code COBTUPDT}.
 */
@SpringBootTest
class TransactionTypeServiceTest {

    @Autowired
    private TransactionTypeService service;

    @Autowired
    private TransactionTypeRepository typeRepository;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void listReturnsSeededData() {
        assertThat(service.listTypes(null, PageRequest.of(0, 100)).getTotalElements())
                .isGreaterThanOrEqualTo(7);
    }

    @Test
    void getTypeIncludesCategories() {
        TransactionTypeDto dto = service.getType("01");
        assertThat(dto.getDescription()).isEqualTo("Purchase");
        assertThat(dto.getCategories()).hasSize(5);
    }

    @Test
    void getMissingTypeThrows() {
        assertThatThrownBy(() -> service.getType("ZZ"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createTypeTrimsDescription() {
        TransactionTypeDto created =
                service.createType(new TransactionTypeDto("09", "  Promo  "));
        assertThat(created.getDescription()).isEqualTo("Promo");
        assertThat(typeRepository.existsById("09")).isTrue();
    }

    @Test
    void createDuplicateTypeThrows() {
        assertThatThrownBy(() -> service.createType(new TransactionTypeDto("01", "Dup")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteTypeWithCategoriesThrows() {
        assertThatThrownBy(() -> service.deleteType("01"))
                .isInstanceOf(ReferentialIntegrityException.class);
    }

    @Test
    void deleteTypeWithoutCategoriesSucceeds() {
        service.deleteCategory("07", "0001");
        service.deleteType("07");
        assertThat(typeRepository.existsById("07")).isFalse();
    }

    @Test
    void categoryCrudLifecycle() {
        service.createCategory("02", new TransactionCategoryDto(null, "0009", "New"));
        assertThat(service.listCategories("02")).hasSize(4);

        service.updateCategory("02", "0009", new TransactionCategoryDto(null, "0009", "Edited"));
        assertThat(service.listCategories("02").stream()
                .anyMatch(c -> c.getDescription().equals("Edited"))).isTrue();

        service.deleteCategory("02", "0009");
        assertThat(service.listCategories("02")).hasSize(3);
    }

    @Test
    void batchProcessesAllOperations() {
        // Remove type 05's only category so it can be deleted (ON DELETE RESTRICT).
        service.deleteCategory("05", "0001");
        List<BatchOperationDto> ops = List.of(
                new BatchOperationDto(BatchOperationDto.Action.INSERT, "10", "Fee"),
                new BatchOperationDto(BatchOperationDto.Action.UPDATE, "02", "Payment v2"),
                new BatchOperationDto(BatchOperationDto.Action.DELETE, "05", null));
        service.processBatch(ops);

        assertThat(service.getType("10").getDescription()).isEqualTo("Fee");
        assertThat(service.getType("02").getDescription()).isEqualTo("Payment v2");
        assertThat(typeRepository.existsById("05")).isFalse();
    }

    @Test
    void batchRollsBackOnFailure() {
        List<BatchOperationDto> ops = List.of(
                new BatchOperationDto(BatchOperationDto.Action.INSERT, "11", "Temp"),
                // Fails: type 01 still has categories (ON DELETE RESTRICT).
                new BatchOperationDto(BatchOperationDto.Action.DELETE, "01", null));

        assertThatThrownBy(() -> service.processBatch(ops))
                .isInstanceOf(ReferentialIntegrityException.class);

        assertThat(typeRepository.existsById("11")).isFalse();
    }
}
