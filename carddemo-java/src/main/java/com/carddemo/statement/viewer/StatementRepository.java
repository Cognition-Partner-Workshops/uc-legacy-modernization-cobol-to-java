package com.carddemo.statement.viewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

@Repository
public class StatementRepository {
    private static final Pattern STATEMENT_FILE = Pattern.compile("^statement-([A-Za-z0-9]+)\\.json$");
    private static final Pattern SAFE_ACCOUNT_ID = Pattern.compile("^[A-Za-z0-9]+$");
    private static final String MOCK_PATTERN = "classpath*:mock-statements/statement-*.json";

    private final ObjectMapper objectMapper;
    private final Path statementsDirectory;

    public StatementRepository(
            ObjectMapper objectMapper,
            @Value("${carddemo.statements.dir:output/statements/}") Path statementsDirectory) {
        this.objectMapper = objectMapper;
        this.statementsDirectory = statementsDirectory;
    }

    public List<Statement> findAll() {
        if (hasExternalStatements()) {
            return readExternalStatements();
        }
        return readMockStatements();
    }

    public Optional<Statement> findByAccountId(String accountId) {
        if (!isSafeAccountId(accountId)) {
            return Optional.empty();
        }
        return findAll().stream().filter(statement -> statement.accountId().equals(accountId)).findFirst();
    }

    private boolean hasExternalStatements() {
        if (!Files.isDirectory(statementsDirectory)) {
            return false;
        }
        try (var files = Files.list(statementsDirectory)) {
            return files.anyMatch(file -> Files.isRegularFile(file)
                    && STATEMENT_FILE.matcher(file.getFileName().toString()).matches());
        } catch (IOException e) {
            return false;
        }
    }

    private List<Statement> readExternalStatements() {
        try (var files = Files.list(statementsDirectory)) {
            return files.filter(Files::isRegularFile)
                    .map(this::readExternalFile)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(Statement::accountId))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Optional<Statement> readExternalFile(Path file) {
        Matcher matcher = STATEMENT_FILE.matcher(file.getFileName().toString());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            Statement statement = objectMapper.readValue(file.toFile(), Statement.class);
            return matcher.group(1).equals(statement.accountId()) ? Optional.of(statement) : Optional.empty();
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private List<Statement> readMockStatements() {
        List<Statement> statements = new ArrayList<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(MOCK_PATTERN);
            for (Resource resource : resources) {
                Optional<Statement> statement = readResource(resource);
                statement.ifPresent(statements::add);
            }
        } catch (IOException ignored) {
            // A missing mock directory should leave the viewer with an empty list.
        }
        return statements.stream().sorted(Comparator.comparing(Statement::accountId)).toList();
    }

    private Optional<Statement> readResource(Resource resource) {
        Matcher matcher = STATEMENT_FILE.matcher(resource.getFilename() == null ? "" : resource.getFilename());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try (InputStream stream = resource.getInputStream()) {
            Statement statement = objectMapper.readValue(stream, Statement.class);
            return matcher.group(1).equals(statement.accountId()) ? Optional.of(statement) : Optional.empty();
        } catch (IOException | RuntimeException ignored) {
            // A malformed mock should not prevent valid statements from being displayed.
            return Optional.empty();
        }
    }

    private boolean isSafeAccountId(String accountId) {
        return accountId != null && SAFE_ACCOUNT_ID.matcher(accountId).matches();
    }
}
