# CardDemo Java statement batch

This is a plain Java 17/Maven port of the `CBSTM03A` statement batch and its
`CBSTM03B` file access routine. From this directory, run:

```sh
mvn test
mvn package -DskipTests
mvn exec:java
```

The generator reads the ASCII fixed-width files in `../app/data/ASCII/` and
writes one JSON statement per account to `output/statements/`. Generated
output is intentionally ignored by Git.
