package com.aws.carddemo.web.inquiry;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import jakarta.jms.Destination;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "carddemo.jms.enabled", havingValue = "true")
public class InquiryJmsListener {
  private final AccountRepository accounts;
  private final JmsTemplate jms;

  public InquiryJmsListener(AccountRepository accounts, JmsTemplate jms) {
    this.accounts = accounts;
    this.jms = jms;
  }

  @JmsListener(destination = "${carddemo.jms.account-request:carddemo.account.request}")
  public void account(String body, jakarta.jms.Message message) throws jakarta.jms.JMSException {
    String requestId = fixed(body, 4, 8);
    String accountNumber = fixed(body, 12, 11);
    String data =
        accounts.findById(parseLong(accountNumber)).map(InquiryJmsListener::accountData).orElse("");
    send(message.getJMSReplyTo(), "ACCT" + fixed(requestId, 8) + fixed(data, 300));
  }

  @JmsListener(destination = "${carddemo.jms.date-request:carddemo.date.request}")
  public void date(String body, jakarta.jms.Message message) throws jakarta.jms.JMSException {
    String requestId = fixed(body, 4, 8);
    send(message.getJMSReplyTo(), "DATE" + fixed(requestId, 8) + LocalDate.now());
  }

  private void send(Destination destination, String value) {
    if (destination != null) jms.convertAndSend(destination, value);
  }

  private static String accountData(Account value) {
    return String.format(
        "ACCOUNT=%-11s STATUS=%-1s BALANCE=%-15s CREDIT-LIMIT=%-15s",
        value.getAcctId(), value.getActiveStatus(), value.getCurrBal(), value.getCreditLimit());
  }

  private static String fixed(String body, int start, int length) {
    if (body == null || body.length() < start) return "";
    return body.substring(start, Math.min(body.length(), start + length)).trim();
  }

  private static String fixed(String body, int length) {
    return String.format("%-" + length + "s", body == null ? "" : body).substring(0, length);
  }

  private static Long parseLong(String value) {
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException exception) {
      return -1L;
    }
  }
}
