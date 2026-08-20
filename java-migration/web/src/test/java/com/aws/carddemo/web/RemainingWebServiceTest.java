package com.aws.carddemo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.bill.BillPaymentService;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.report.ReportService;
import com.aws.carddemo.web.transaction.TransactionService;
import com.aws.carddemo.web.user.UserAdminService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class RemainingWebServiceTest {
  private static final Context CONTEXT =
      new Context("", "", "", "", "ADMIN001", "A", 1L, "4000000000000001");

  @Mock UsrsecRepository users;
  @Mock JobLauncher launcher;
  @Mock Job reportJob;

  @Test
  void addUserRejectsDuplicateWithCobolMessage() {
    UserAdminService service = new UserAdminService(users);
    when(users.existsById("NEWUSER")).thenReturn(true);
    var result =
        service.add(
            new UserAdminService.UserAddRequest(
                "First", "Last", "newuser", "PASSWORD", "U", CONTEXT));
    assertEquals("User ID already exist...", result.message());
  }

  @Test
  void updateUserAndDeleteUserRoundTripUsesLegacyFields() {
    Usrsec user = new Usrsec();
    user.setSecUsrId("USER001");
    when(users.findById("USER001")).thenReturn(Optional.of(user));
    when(users.existsById("USER001")).thenReturn(true);
    when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UserAdminService service = new UserAdminService(users);
    var updated =
        service.update(
            new UserAdminService.UserUpdateRequest(
                "USER001", "New", "Name", "PASSWORD", "U", true, CONTEXT));
    assertEquals("New", updated.data().getSecUsrFname());
    var deleted = service.delete(new UserAdminService.UserDeleteRequest("USER001", true, CONTEXT));
    assertEquals("", deleted.message());
    verify(users).deleteById("USER001");
  }

  @Test
  void billPaymentRejectsZeroBalance() {
    var accounts = mock(com.aws.carddemo.domain.AccountRepository.class);
    var xrefs = mock(com.aws.carddemo.domain.CardXrefRepository.class);
    var transactions = mock(com.aws.carddemo.domain.TransactionRepository.class);
    var account = new com.aws.carddemo.domain.Account();
    account.setCurrBal(BigDecimal.ZERO);
    when(accounts.findById(1L)).thenReturn(Optional.of(account));
    var result =
        new BillPaymentService(accounts, xrefs, transactions)
            .pay(new BillPaymentService.BillPaymentRequest(1L, true, CONTEXT));
    assertEquals("You have nothing to pay...", result.message());
  }

  @Test
  void transactionViewRejectsMissingId() {
    var transactions = mock(com.aws.carddemo.domain.TransactionRepository.class);
    var xrefs = mock(com.aws.carddemo.domain.CardXrefRepository.class);
    var result = new TransactionService(transactions, xrefs).view("", CONTEXT);
    assertEquals("Tran ID can NOT be empty...", result.message());
  }

  @Test
  void reportSubmissionReturnsSubmittedWithoutBlocking() throws Exception {
    var service = new ReportService(launcher, reportJob);
    var result =
        service.submit(
            new ReportService.ReportRequest("monthly", "2022-01-01", "2022-01-31", true, CONTEXT));
    assertEquals("SUBMITTED", result.data().status());
  }
}
