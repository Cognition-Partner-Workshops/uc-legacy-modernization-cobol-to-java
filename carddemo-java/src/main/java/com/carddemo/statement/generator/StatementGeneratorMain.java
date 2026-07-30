package com.carddemo.statement.generator;

import java.nio.file.Path;

public final class StatementGeneratorMain {
  private StatementGeneratorMain() {}
  public static void main(String[] args) throws Exception {
    Path data = args.length > 0 ? Path.of(args[0]) : Path.of("../app/data/ASCII");
    Path output = args.length > 1 ? Path.of(args[1]) : Path.of("output/statements");
    var report = new StatementGenerator().generate(data, output);
    System.out.printf("Generated %d statements from %d transactions.%n", report.statements(), report.transactions());
    if (!report.anomalies().isEmpty()) {
      System.out.println("Data anomalies:");
      report.anomalies().forEach(a -> System.out.println(" - " + a));
    }
  }
}
