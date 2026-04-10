package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.DateValidationService;
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
 * Mirrors COTRN02C.cbl - Transaction Add.
 * Replicates full validation from VALIDATE-INPUT-DATA-FIELDS (lines 235-437).
 */
@Controller
public class TransactionAddController {

    private static final String PGM_NAME = "COTRN02C";
    private static final String TRAN_ID = "CT02";

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DateValidationService dateValidationService;
    private final TransactionIdService transactionIdService;

    public TransactionAddController(TransactionRepository transactionRepository,
                                    CardXrefRepository cardXrefRepository,
                                    DateValidationService dateValidationService,
                                    TransactionIdService transactionIdService) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.dateValidationService = dateValidationService;
        this.transactionIdService = transactionIdService;
    }

    @GetMapping("/transaction/add")
    public String showTransactionAdd(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        return "tran-add";
    }

    @PostMapping("/transaction/add")
    public String processTransactionAdd(
            @RequestParam(defaultValue = "") String acctId,
            @RequestParam(defaultValue = "") String cardNum,
            @RequestParam(defaultValue = "") String typeCd,
            @RequestParam(defaultValue = "") String catCd,
            @RequestParam(defaultValue = "") String source,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String amount,
            @RequestParam(defaultValue = "") String origDate,
            @RequestParam(defaultValue = "") String procDate,
            @RequestParam(defaultValue = "") String merchantId,
            @RequestParam(defaultValue = "") String merchantName,
            @RequestParam(defaultValue = "") String merchantCity,
            @RequestParam(defaultValue = "") String merchantZip,
            @RequestParam(defaultValue = "") String confirm,
            @RequestParam(defaultValue = "") String action,
            Model model, HttpSession session) {

        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) {
            String fromPgm = commarea.getFromProgram();
            if (fromPgm == null || fromPgm.isBlank()) {
                return "redirect:/menu";
            }
            return "redirect:/menu";
        }
        if ("PF4".equals(action)) {
            return "redirect:/transaction/add";
        }

        populateHeaderInfo(model);
        preserveFormData(model, acctId, cardNum, typeCd, catCd, source, description,
            amount, origDate, procDate, merchantId, merchantName, merchantCity, merchantZip);

        // VALIDATE-INPUT-KEY-FIELDS - mirrors lines 193-230
        String resolvedCardNum = cardNum.trim();
        String resolvedAcctId = acctId.trim();

        if (!resolvedAcctId.isBlank()) {
            if (!resolvedAcctId.matches("\\d+")) {
                model.addAttribute("errorMessage", "Account ID must be Numeric...");
                return "tran-add";
            }
            long acctIdNum = Long.parseLong(resolvedAcctId);
            Optional<CardXref> xref = cardXrefRepository.findByXrefAcctId(acctIdNum);
            if (xref.isEmpty()) {
                model.addAttribute("errorMessage", "Account not found in cross reference...");
                return "tran-add";
            }
            resolvedCardNum = xref.get().getXrefCardNum();
            model.addAttribute("cardNum", resolvedCardNum);
        } else if (!resolvedCardNum.isBlank()) {
            if (!resolvedCardNum.matches("\\d+")) {
                model.addAttribute("errorMessage", "Card Number must be Numeric...");
                return "tran-add";
            }
            Optional<CardXref> xref = cardXrefRepository.findByXrefCardNum(resolvedCardNum);
            if (xref.isEmpty()) {
                model.addAttribute("errorMessage", "Card not found in cross reference...");
                return "tran-add";
            }
            resolvedAcctId = String.valueOf(xref.get().getXrefAcctId());
            model.addAttribute("acctId", resolvedAcctId);
        } else {
            model.addAttribute("errorMessage", "Account or Card Number must be entered...");
            return "tran-add";
        }

        // VALIDATE-INPUT-DATA-FIELDS - mirrors lines 235-437
        String error = validateDataFields(typeCd, catCd, source, description, amount,
            origDate, procDate, merchantId, merchantName, merchantCity, merchantZip);
        if (error != null) {
            model.addAttribute("errorMessage", error);
            return "tran-add";
        }

        // Confirm handling - mirrors lines 169-188
        if ("Y".equalsIgnoreCase(confirm) || "y".equals(confirm)) {
            // ADD-TRANSACTION - generate next TRAN-ID and save atomically
            Transaction tran = new Transaction();
            tran.setTranTypeCd(typeCd.trim());
            tran.setTranCatCd(Integer.parseInt(catCd.trim()));
            tran.setTranSource(source.trim());
            tran.setTranDesc(description.trim());
            tran.setTranAmt(new BigDecimal(amount.trim()));
            tran.setTranCardNum(resolvedCardNum);
            tran.setTranOrigTs(origDate.trim());
            tran.setTranProcTs(procDate.trim());
            tran.setTranMerchantId(Long.parseLong(merchantId.trim()));
            tran.setTranMerchantName(merchantName.trim());
            tran.setTranMerchantCity(merchantCity.trim());
            tran.setTranMerchantZip(merchantZip.trim());

            Transaction saved = transactionIdService.generateIdAndSave(tran);
            model.addAttribute("errorMessage", "Transaction added successfully. ID: " + saved.getTranId());
        } else if ("N".equalsIgnoreCase(confirm)) {
            model.addAttribute("errorMessage", "Transaction cancelled.");
        } else {
            model.addAttribute("errorMessage", "Confirm to add this transaction...");
        }

        return "tran-add";
    }

    private String validateDataFields(String typeCd, String catCd, String source,
            String description, String amount, String origDate, String procDate,
            String merchantId, String merchantName, String merchantCity, String merchantZip) {

        if (typeCd.isBlank()) return "Type CD can NOT be empty...";
        if (catCd.isBlank()) return "Category CD can NOT be empty...";
        if (source.isBlank()) return "Source can NOT be empty...";
        if (description.isBlank()) return "Description can NOT be empty...";
        if (amount.isBlank()) return "Amount can NOT be empty...";
        if (origDate.isBlank()) return "Orig Date can NOT be empty...";
        if (procDate.isBlank()) return "Proc Date can NOT be empty...";
        if (merchantId.isBlank()) return "Merchant ID can NOT be empty...";
        if (merchantName.isBlank()) return "Merchant Name can NOT be empty...";
        if (merchantCity.isBlank()) return "Merchant City can NOT be empty...";
        if (merchantZip.isBlank()) return "Merchant Zip can NOT be empty...";

        // Type/Category must be numeric
        if (!typeCd.trim().matches("\\d+")) return "Type CD must be Numeric...";
        if (!catCd.trim().matches("\\d+")) return "Category CD must be Numeric...";

        // Amount format: -99999999.99
        String amtTrimmed = amount.trim();
        if (!amtTrimmed.matches("[+-]?\\d{1,8}\\.\\d{2}")) {
            return "Amount should be in format -99999999.99";
        }

        // Date format: YYYY-MM-DD
        if (!origDate.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "Orig Date should be in format YYYY-MM-DD";
        }
        DateValidationService.ValidationResult origResult =
            dateValidationService.validateYyyyMmDd(origDate.trim());
        if (!origResult.isValid()) return "Orig Date is invalid: " + origResult.message();

        if (!procDate.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "Proc Date should be in format YYYY-MM-DD";
        }
        DateValidationService.ValidationResult procResult =
            dateValidationService.validateYyyyMmDd(procDate.trim());
        if (!procResult.isValid()) return "Proc Date is invalid: " + procResult.message();

        // Merchant ID must be numeric
        if (!merchantId.trim().matches("\\d+")) return "Merchant ID must be Numeric...";

        return null;
    }

    private void preserveFormData(Model model, String acctId, String cardNum,
            String typeCd, String catCd, String source, String description,
            String amount, String origDate, String procDate, String merchantId,
            String merchantName, String merchantCity, String merchantZip) {
        model.addAttribute("acctId", acctId);
        model.addAttribute("cardNum", cardNum);
        model.addAttribute("typeCd", typeCd);
        model.addAttribute("catCd", catCd);
        model.addAttribute("source", source);
        model.addAttribute("description", description);
        model.addAttribute("amount", amount);
        model.addAttribute("origDate", origDate);
        model.addAttribute("procDate", procDate);
        model.addAttribute("merchantId", merchantId);
        model.addAttribute("merchantName", merchantName);
        model.addAttribute("merchantCity", merchantCity);
        model.addAttribute("merchantZip", merchantZip);
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Transaction Add");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
