package com.aws.carddemo.dataload;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataRunner implements ApplicationRunner {
  private final SeedDataLoader loader;

  public SeedDataRunner(SeedDataLoader loader) {
    this.loader = loader;
  }

  @Override
  public void run(ApplicationArguments arguments) {
    if (!arguments.containsOption("dataset")) {
      return;
    }
    String selector = arguments.getOptionValues("dataset").get(0);
    if ("all".equalsIgnoreCase(selector)) {
      loader.loadAll();
      return;
    }
    loader.load(SeedDataset.valueOf(selector.trim().toUpperCase().replace('-', '_')));
  }
}
