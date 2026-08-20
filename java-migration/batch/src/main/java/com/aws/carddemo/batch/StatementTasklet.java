package com.aws.carddemo.batch;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class StatementTasklet implements Tasklet {
  private final CardXrefRepository xrefs;
  private final CustomerRepository customers;
  private final AccountRepository accounts;
  private final TransactionRepository transactions;
  private final String textPath;
  private final String htmlPath;

  StatementTasklet(
      CardXrefRepository xrefs,
      CustomerRepository customers,
      AccountRepository accounts,
      TransactionRepository transactions,
      @Value("${carddemo.batch.statement-path:target/output/statements.txt}") String textPath,
      @Value("${carddemo.batch.statement-html-path:target/output/statements.html}")
          String htmlPath) {
    this.xrefs = xrefs;
    this.customers = customers;
    this.accounts = accounts;
    this.transactions = transactions;
    this.textPath = textPath;
    this.htmlPath = htmlPath;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws IOException {
    var parameters = context.getStepContext().getStepExecution().getJobParameters();
    Path text = Paths.get(BatchSupport.parameter(parameters, "statementPath", textPath));
    Path html = Paths.get(BatchSupport.parameter(parameters, "statementHtmlPath", htmlPath));
    Files.createDirectories(text.toAbsolutePath().getParent());
    Files.createDirectories(html.toAbsolutePath().getParent());
    StringBuilder plain = new StringBuilder();
    StringBuilder markup = new StringBuilder();
    for (CardXref xref :
        xrefs.findAll().stream()
            .sorted((a, b) -> a.getId().getCardNum().compareTo(b.getId().getCardNum()))
            .toList()) {
      Customer customer = customers.findById(xref.getCustId()).orElseThrow();
      Account account = accounts.findById(xref.getAcctId()).orElseThrow();
      List<Transaction> cardTransactions =
          transactions.findByCardNumOrderByTranId(xref.getId().getCardNum());
      BigDecimal total = BigDecimal.ZERO;
      String name =
          String.join(
                  " ",
                  List.of(
                      customer.getFirstName(), customer.getMiddleName(), customer.getLastName()))
              .replaceAll("\\s+", " ")
              .trim();
      String address3 =
          String.join(
                  " ",
                  List.of(
                      customer.getAddrLine3(),
                      customer.getAddrStateCd(),
                      customer.getAddrCountryCd(),
                      customer.getAddrZip()))
              .replaceAll("\\s+", " ")
              .trim();
      plain
          .append("*".repeat(31))
          .append("START OF STATEMENT")
          .append("*".repeat(31))
          .append('\n')
          .append(BatchSupport.pad(name, 75))
          .append('\n')
          .append(BatchSupport.pad(customer.getAddrLine1(), 50))
          .append('\n')
          .append(BatchSupport.pad(customer.getAddrLine2(), 50))
          .append('\n')
          .append(BatchSupport.pad(address3, 80))
          .append('\n')
          .append("-".repeat(80))
          .append('\n')
          .append("                                 Basic Details")
          .append('\n')
          .append("-".repeat(80))
          .append('\n')
          .append(String.format("Account ID         :%-20s%n", account.getAcctId()))
          .append(String.format("Current Balance    :%s%n", money(account.getCurrBal())))
          .append(String.format("FICO Score         :%-20s%n", customer.getFicoCreditScore()))
          .append("-".repeat(80))
          .append('\n')
          .append("                              TRANSACTION SUMMARY ")
          .append('\n')
          .append("-".repeat(80))
          .append('\n')
          .append(
              "Tran ID         Tran Details                                           Tran Amount\n");
      markup
          .append(
              "<!DOCTYPE html>\n<html lang=\"en\">\n<head><meta charset=\"utf-8\"><title>HTML Table Layout</title></head>\n")
          .append("<body><h2>Bank of XYZ</h2><h3>Account ")
          .append(account.getAcctId())
          .append("</h3><p>")
          .append(escape(name))
          .append("</p><p>")
          .append(escape(customer.getAddrLine1()))
          .append("</p><p>")
          .append(escape(customer.getAddrLine2()))
          .append("</p><p>")
          .append(escape(address3))
          .append("</p><h4>Basic Details</h4><p>Account ID: ")
          .append(account.getAcctId())
          .append("</p><p>Current Balance: ")
          .append(money(account.getCurrBal()))
          .append("</p><p>FICO Score: ")
          .append(customer.getFicoCreditScore())
          .append("</p><h4>TRANSACTION SUMMARY</h4><table>\n");
      for (Transaction transaction : cardTransactions) {
        BigDecimal amount = BatchSupport.value(transaction.getAmt());
        total = total.add(amount);
        plain.append(
            String.format(
                "%-16s %-49s $%10.2f%n",
                transaction.getTranId(), transaction.getTranDesc(), amount));
        markup
            .append("<tr><td>")
            .append(escape(transaction.getTranId()))
            .append("</td><td>")
            .append(escape(transaction.getTranDesc()))
            .append("</td><td>$")
            .append(money(amount))
            .append("</td></tr>\n");
      }
      plain
          .append("-".repeat(80))
          .append('\n')
          .append(String.format("Total EXP:%56s$%10.2f%n", "", total))
          .append("*".repeat(32))
          .append("END OF STATEMENT")
          .append("*".repeat(32))
          .append('\n');
      markup
          .append("</table><p>Total EXP: $")
          .append(money(total))
          .append("</p><p>END OF STATEMENT</p></body></html>\n");
    }
    Files.writeString(text, plain.toString());
    Files.writeString(html, markup.toString());
    contribution.incrementWriteCount((int) xrefs.count());
    return RepeatStatus.FINISHED;
  }

  private String money(BigDecimal amount) {
    return String.format("%,.2f", BatchSupport.value(amount));
  }

  private String escape(String value) {
    return value == null
        ? ""
        : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
