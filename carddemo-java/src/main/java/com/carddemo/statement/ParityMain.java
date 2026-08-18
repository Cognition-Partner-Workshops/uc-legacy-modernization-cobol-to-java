package com.carddemo.statement;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point: {@code java -jar target/carddemo-java-1.0.0-SNAPSHOT.jar [repoRoot]}.
 * Generates the Java statements, compares them with the COBOL reference output and writes
 * {@code carddemo-java/target/parity/parity-summary.json}.
 */
public final class ParityMain {

    private ParityMain() {
    }

    public static void main(String[] args) {
        Path repoRoot = args.length > 0 ? Path.of(args[0]) : findRepoRoot();
        Path summary = new ParityRunner(repoRoot).run();
        System.out.println("parity summary: " + summary);
    }

    private static Path findRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve("app/data/ASCII"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root containing app/data/ASCII");
    }
}
