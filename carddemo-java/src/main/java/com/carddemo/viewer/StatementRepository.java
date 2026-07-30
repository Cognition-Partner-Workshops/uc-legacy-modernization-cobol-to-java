package com.carddemo.viewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class StatementRepository {
    private static final Logger log = LoggerFactory.getLogger(StatementRepository.class);
    private final ObjectMapper objectMapper;
    private final Path statementsDirectory;

    public StatementRepository(
            ObjectMapper objectMapper,
            @Value("${carddemo.statements.dir:output/statements}") String statementsDirectory) {
        this.objectMapper = objectMapper;
        this.statementsDirectory = Path.of(statementsDirectory);
    }

    public List<Statement> findAll() {
        if (!Files.isDirectory(statementsDirectory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(statementsDirectory)) {
            return files.filter(this::isStatementFile)
                    .map(this::readStatement)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(Statement::accountId)
                            .thenComparing(Statement::cardNumber))
                    .toList();
        } catch (IOException exception) {
            log.warn("Unable to scan statement directory {}", statementsDirectory, exception);
            return List.of();
        }
    }

    public Optional<Statement> findByAccountId(String accountId) {
        return findAll().stream().filter(statement -> statement.accountId().equals(accountId)).findFirst();
    }

    private boolean isStatementFile(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().startsWith("statement-")
                && path.getFileName().toString().endsWith(".json");
    }

    private Optional<Statement> readStatement(Path path) {
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), Statement.class));
        } catch (IOException | RuntimeException exception) {
            log.warn("Skipping unreadable statement file {}", path, exception);
            return Optional.empty();
        }
    }
}
