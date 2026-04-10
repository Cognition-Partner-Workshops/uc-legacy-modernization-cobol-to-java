package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionIdService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Mirrors COBIL00C.cbl - Bill Payment.
 */
@Controller
public class BillPaymentController {

    private static final String PGM_NAME = "COBIL00C";
    private static final String TRAN_ID = "CB00";

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdService transactionIdService;

    public BillPaymentController(AccountRepository accountRepository,
                                 CardXrefRepository cardXrefRepository,
                                 TransactionRepository transactionRepository,
                                 TransactionIdService transactionIdService) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.transactionIdService = transactionIdService;
    }

    @GetMapping("/bill-payment")
    public String showBillPayment(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        return "bill-payment";
    }

    @PostMapping("/bill-payment")
    public String processBillPayment(@RequestParam(defaultValue = "") String acctId,
                                     @RequestParam(defaultValue = "") String amount,
                                     @RequestParam(defaultValue = "") String confirm,
                                     @RequestParam(defaultValue = "") String action,
                                     Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) return "redirect:/menu";

        populateHeaderInfo(model);
        model.addAttribute("acctId", acctId);
        model.addAttribute("amount", amount);

        if (acctId.isBlank()) {
            model.addAttribute("errorMessage", "Please enter an Account ID...");
            return "bill-payment";
        }
        if (amount.isBlank()) {
            model.addAttribute("errorMessage", "Please enter a payment Amount...");
            return "bill-payment";
        }

        long acctIdNum;
        BigDecimal paymentAmt;
        try {
            acctIdNum = Long.parseLong(acctId.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Account ID must be numeric...");
            return "bill-payment";
        }
        try {
            paymentAmt = new BigDecimal(amount.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Amount must be numeric...");
            return "bill-payment";
        }

        Optional<Account> acctOpt = accountRepository.findById(acctIdNum);
        if (acctOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found...");
            return "bill-payment";
        }

        if ("Y".equalsIgnoreCase(confirm)) {
            if (paymentAmt.compareTo(BigDecimal.ZERO) <= 0) {
                model.addAttribute("errorMessage", "Payment amount must be positive...");
                return "bill-payment";
            }
            Account account = acctOpt.get();
            BigDecimal currentBal = account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO;
            account.setAcctCurrBal(currentBal.subtract(paymentAmt));
            accountRepository.save(account);

            // Create payment transaction
            String nextId = transactionIdService.generateNextTranId();

            Transaction tran = new Transaction();
            tran.setTranId(nextId);
            tran.setTranTypeCd("02");
            tran.setTranCatCd(1);
            tran.setTranSource("ONLINE");
            tran.setTranDesc("Bill Payment");
            tran.setTranAmt(paymentAmt);
            Optional<CardXref> xref = cardXrefRepository.findByXrefAcctId(acctIdNum);
            xref.ifPresent(x -> tran.setTranCardNum(x.getXrefCardNum()));
            tran.setTranOrigTs(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")));
            tran.setTranProcTs(tran.getTranOrigTs());
            transactionRepository.save(tran);

            model.addAttribute("errorMessage", "Payment processed successfully...");
        } else {
            model.addAttribute("errorMessage", "Confirm payment (Y/N)...");
        }

        return "bill-payment";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Bill Payment");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
