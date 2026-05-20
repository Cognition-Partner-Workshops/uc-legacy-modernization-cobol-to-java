package com.carddemo.dataloader.config;

import com.carddemo.dataloader.parser.FixedWidthParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Map;

@Configuration
public class BatchConfig {

    @Value("${data.dir:../app/data/ASCII}")
    private String dataDir;

    @Bean
    public Job dataLoaderJob(JobRepository jobRepository,
                             Step loadAccounts, Step loadCards, Step loadCardXref,
                             Step loadCustomers, Step loadDailyTrans,
                             Step loadDiscGroups, Step loadTcatBal,
                             Step loadTranCatg, Step loadTranType,
                             Step seedUsers) {
        return new JobBuilder("dataLoaderJob", jobRepository)
                .start(seedUsers)
                .next(loadAccounts)
                .next(loadCards)
                .next(loadCardXref)
                .next(loadCustomers)
                .next(loadDailyTrans)
                .next(loadDiscGroups)
                .next(loadTcatBal)
                .next(loadTranCatg)
                .next(loadTranType)
                .build();
    }

    // --- Seed Users ---
    @Bean
    public Step seedUsers(JobRepository jobRepository, PlatformTransactionManager txManager,
                          DataSource dataSource) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String adminHash = encoder.encode("PASSWORD");
        String userHash = encoder.encode("PASSWORD");

        return new StepBuilder("seedUsers", jobRepository)
                .<Map<String, String>, Map<String, String>>chunk(10, txManager)
                .reader(new org.springframework.batch.item.support.ListItemReader<>(
                        java.util.List.of(
                                Map.of("id", "ADMIN001", "fname", "Admin", "lname", "User", "hash", adminHash, "type", "A"),
                                Map.of("id", "USER0001", "fname", "Regular", "lname", "User", "hash", userHash, "type", "U")
                        )))
                .writer(items -> {
                    var jdbcWriter = new JdbcBatchItemWriterBuilder<Map<String, String>>()
                            .dataSource(dataSource)
                            .sql("INSERT INTO auth.users (user_id, first_name, last_name, password_hash, user_type) " +
                                 "VALUES (:id, :fname, :lname, :hash, :type) ON CONFLICT DO NOTHING")
                            .columnMapped()
                            .build();
                    jdbcWriter.afterPropertiesSet();
                    jdbcWriter.write(items);
                })
                .build();
    }

    // --- Accounts (CVACT01Y, 300 chars) ---
    @Bean
    public Step loadAccounts(JobRepository jobRepository, PlatformTransactionManager txManager,
                             DataSource dataSource) {
        return new StepBuilder("loadAccounts", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("acctdata.txt"))
                .writer(accountWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> accountWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO account.accounts (acct_id, active_status, curr_bal, credit_limit, " +
                     "cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, " +
                     "curr_cyc_debit, addr_zip, group_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 11));
                    ps.setString(2, FixedWidthParser.extractString(line, 11, 1));
                    ps.setBigDecimal(3, FixedWidthParser.extractSignedDecimal(line, 12, 12, 2));
                    ps.setBigDecimal(4, FixedWidthParser.extractSignedDecimal(line, 24, 12, 2));
                    ps.setBigDecimal(5, FixedWidthParser.extractSignedDecimal(line, 36, 12, 2));
                    ps.setString(6, FixedWidthParser.extractString(line, 48, 10));
                    ps.setString(7, FixedWidthParser.extractString(line, 58, 10));
                    ps.setString(8, FixedWidthParser.extractString(line, 68, 10));
                    ps.setBigDecimal(9, FixedWidthParser.extractSignedDecimal(line, 78, 12, 2));
                    ps.setBigDecimal(10, FixedWidthParser.extractSignedDecimal(line, 90, 12, 2));
                    ps.setString(11, FixedWidthParser.extractString(line, 102, 10));
                    ps.setString(12, FixedWidthParser.extractString(line, 112, 10));
                })
                .build();
    }

    // --- Cards (CVACT02Y, 150 chars) ---
    @Bean
    public Step loadCards(JobRepository jobRepository, PlatformTransactionManager txManager,
                          DataSource dataSource) {
        return new StepBuilder("loadCards", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("carddata.txt"))
                .writer(cardWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> cardWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO card.cards (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status) " +
                     "VALUES (?,?,?,?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 16));
                    ps.setString(2, FixedWidthParser.extractString(line, 16, 11));
                    ps.setInt(3, FixedWidthParser.extractInt(line, 27, 3));
                    ps.setString(4, FixedWidthParser.extractString(line, 30, 50));
                    ps.setString(5, FixedWidthParser.extractString(line, 80, 10));
                    ps.setString(6, FixedWidthParser.extractString(line, 90, 1));
                })
                .build();
    }

    // --- Card XREF (CVACT03Y, 50 chars) ---
    @Bean
    public Step loadCardXref(JobRepository jobRepository, PlatformTransactionManager txManager,
                              DataSource dataSource) {
        return new StepBuilder("loadCardXref", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("cardxref.txt"))
                .writer(cardXrefWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> cardXrefWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO card.card_xref (card_num, cust_id, acct_id) VALUES (?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 16));
                    ps.setString(2, FixedWidthParser.extractString(line, 16, 9));
                    ps.setString(3, FixedWidthParser.extractString(line, 25, 11));
                })
                .build();
    }

    // --- Customers (CVCUS01Y, 500 chars) ---
    @Bean
    public Step loadCustomers(JobRepository jobRepository, PlatformTransactionManager txManager,
                               DataSource dataSource) {
        return new StepBuilder("loadCustomers", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("custdata.txt"))
                .writer(customerWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> customerWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO customer.customers (cust_id, first_name, middle_name, last_name, " +
                     "addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, " +
                     "phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, " +
                     "pri_card_holder_ind, fico_credit_score) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?::date,?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 9));
                    ps.setString(2, FixedWidthParser.extractString(line, 9, 25));
                    ps.setString(3, FixedWidthParser.extractString(line, 34, 25));
                    ps.setString(4, FixedWidthParser.extractString(line, 59, 25));
                    ps.setString(5, FixedWidthParser.extractString(line, 84, 50));
                    ps.setString(6, FixedWidthParser.extractString(line, 134, 50));
                    ps.setString(7, FixedWidthParser.extractString(line, 184, 50));
                    ps.setString(8, FixedWidthParser.extractString(line, 234, 2));
                    ps.setString(9, FixedWidthParser.extractString(line, 236, 3));
                    ps.setString(10, FixedWidthParser.extractString(line, 239, 10));
                    ps.setString(11, FixedWidthParser.extractString(line, 249, 15));
                    ps.setString(12, FixedWidthParser.extractString(line, 264, 15));
                    ps.setString(13, FixedWidthParser.extractString(line, 279, 9));
                    ps.setString(14, FixedWidthParser.extractString(line, 288, 20));
                    String dob = FixedWidthParser.extractString(line, 308, 10);
                    ps.setString(15, dob.isEmpty() ? null : dob);
                    ps.setString(16, FixedWidthParser.extractString(line, 318, 10));
                    ps.setString(17, FixedWidthParser.extractString(line, 328, 1));
                    ps.setInt(18, FixedWidthParser.extractInt(line, 329, 3));
                })
                .build();
    }

    // --- Daily Transactions (CVTRA06Y, 350 chars) ---
    @Bean
    public Step loadDailyTrans(JobRepository jobRepository, PlatformTransactionManager txManager,
                                DataSource dataSource) {
        return new StepBuilder("loadDailyTrans", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("dailytran.txt"))
                .writer(dailyTranWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> dailyTranWriter(DataSource dataSource) {
        return transactionWriterHelper(dataSource,
                "INSERT INTO transaction.daily_transactions (tran_id, type_cd, cat_cd, source, description, " +
                "amount, merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts, proc_ts) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING");
    }

    // --- Disclosure Groups (CVTRA02Y, 50 chars) ---
    @Bean
    public Step loadDiscGroups(JobRepository jobRepository, PlatformTransactionManager txManager,
                                DataSource dataSource) {
        return new StepBuilder("loadDiscGroups", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("discgrp.txt"))
                .writer(discGroupWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> discGroupWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO transaction.disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, int_rate) " +
                     "VALUES (?,?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 10));
                    ps.setString(2, FixedWidthParser.extractString(line, 10, 2));
                    ps.setInt(3, FixedWidthParser.extractInt(line, 12, 4));
                    ps.setBigDecimal(4, FixedWidthParser.extractSignedDecimal(line, 16, 6, 2));
                })
                .build();
    }

    // --- Tran Cat Balance (CVTRA01Y, 50 chars) ---
    @Bean
    public Step loadTcatBal(JobRepository jobRepository, PlatformTransactionManager txManager,
                             DataSource dataSource) {
        return new StepBuilder("loadTcatBal", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("tcatbal.txt"))
                .writer(tcatBalWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> tcatBalWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO transaction.tran_cat_balances (acct_id, type_cd, cat_cd, balance) " +
                     "VALUES (?,?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 11));
                    ps.setString(2, FixedWidthParser.extractString(line, 11, 2));
                    ps.setInt(3, FixedWidthParser.extractInt(line, 13, 4));
                    ps.setBigDecimal(4, FixedWidthParser.extractSignedDecimal(line, 17, 11, 2));
                })
                .build();
    }

    // --- Tran Categories (CVTRA04Y, 60 chars) ---
    @Bean
    public Step loadTranCatg(JobRepository jobRepository, PlatformTransactionManager txManager,
                              DataSource dataSource) {
        return new StepBuilder("loadTranCatg", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("trancatg.txt"))
                .writer(tranCatgWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> tranCatgWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO transaction.tran_categories (type_cd, cat_cd, cat_desc) " +
                     "VALUES (?,?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 2));
                    ps.setInt(2, FixedWidthParser.extractInt(line, 2, 4));
                    ps.setString(3, FixedWidthParser.extractString(line, 6, 50));
                })
                .build();
    }

    // --- Tran Types (CVTRA03Y, 60 chars) ---
    @Bean
    public Step loadTranType(JobRepository jobRepository, PlatformTransactionManager txManager,
                              DataSource dataSource) {
        return new StepBuilder("loadTranType", jobRepository)
                .<String, String>chunk(100, txManager)
                .reader(lineReader("trantype.txt"))
                .writer(tranTypeWriter(dataSource))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<String> tranTypeWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql("INSERT INTO transaction.tran_types (tran_type, type_desc) VALUES (?,?) ON CONFLICT DO NOTHING")
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 2));
                    ps.setString(2, FixedWidthParser.extractString(line, 2, 50));
                })
                .build();
    }

    // --- Helper for transaction-layout writers ---
    private JdbcBatchItemWriter<String> transactionWriterHelper(DataSource dataSource, String sql) {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(dataSource)
                .sql(sql)
                .itemPreparedStatementSetter((line, ps) -> {
                    ps.setString(1, FixedWidthParser.extractString(line, 0, 16));
                    ps.setString(2, FixedWidthParser.extractString(line, 16, 2));
                    ps.setInt(3, FixedWidthParser.extractInt(line, 18, 4));
                    ps.setString(4, FixedWidthParser.extractString(line, 22, 10));
                    ps.setString(5, FixedWidthParser.extractString(line, 32, 100));
                    ps.setBigDecimal(6, FixedWidthParser.extractSignedDecimal(line, 132, 11, 2));
                    ps.setString(7, FixedWidthParser.extractString(line, 143, 9));
                    ps.setString(8, FixedWidthParser.extractString(line, 152, 50));
                    ps.setString(9, FixedWidthParser.extractString(line, 202, 50));
                    ps.setString(10, FixedWidthParser.extractString(line, 252, 10));
                    ps.setString(11, FixedWidthParser.extractString(line, 262, 16));
                    ps.setString(12, FixedWidthParser.extractString(line, 278, 26));
                    ps.setString(13, FixedWidthParser.extractString(line, 304, 26));
                })
                .build();
    }

    private FlatFileItemReader<String> lineReader(String filename) {
        return new FlatFileItemReaderBuilder<String>()
                .name(filename + "Reader")
                .resource(new FileSystemResource(dataDir + "/" + filename))
                .lineMapper((line, lineNumber) -> line)
                .build();
    }
}
