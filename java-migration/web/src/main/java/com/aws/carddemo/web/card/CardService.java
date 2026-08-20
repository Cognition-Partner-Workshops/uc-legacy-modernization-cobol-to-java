package com.aws.carddemo.web.card;

import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {
  private final CardRepository cards;

  public CardService(CardRepository cards) {
    this.cards = cards;
  }

  public Response<CardPage> list(CardListRequest request) {
    if (request.accountId() != null
        && (request.accountId() <= 0 || request.accountId().toString().length() != 11)) {
      return Response.error(
          "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER", "accountId", request.context());
    }
    String filter = request.cardNumber() == null ? "" : request.cardNumber().trim();
    if (!filter.isEmpty() && !filter.matches("\\d{16}")) {
      return Response.error(
          "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER", "cardNumber", request.context());
    }
    List<Card> all =
        cards.findAll(Sort.by(Sort.Direction.ASC, "cardNum")).stream()
            .filter(c -> filter.isEmpty() || c.getCardNum().equals(filter))
            .filter(
                c ->
                    request.accountId() == null
                        || Objects.equals(c.getAcctId(), request.accountId()))
            .toList();
    int pageSize = 7;
    int page = Math.max(0, request.page());
    if ("BACK".equalsIgnoreCase(request.direction())) {
      page = Math.max(0, page - 1);
    }
    int from = Math.min(page * pageSize, all.size());
    int to = Math.min(from + pageSize, all.size());
    return Response.ok(
        new CardPage(all.subList(from, to), page, pageSize, to < all.size(), page > 0),
        "COCRDLIC",
        request.context());
  }

  public Response<Card> detail(String cardNumber, Context context) {
    if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
      return Response.error(
          "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER", "cardNumber", context);
    }
    Card card = cards.findById(cardNumber).orElse(null);
    return card == null
        ? Response.error(
            "Card:" + cardNumber + " not found in Card Master file.", "cardNumber", context)
        : Response.ok(card, "COCRDSLC", withCard(context, cardNumber, "COCRDSLC"));
  }

  @Transactional
  public Response<Card> update(String cardNumber, CardUpdateRequest request) {
    Card card = cards.findById(cardNumber).orElse(null);
    if (card == null)
      return Response.error(
          "Card:" + cardNumber + " not found in Card Master file.",
          "cardNumber",
          request.context());
    if (blank(request.embossedName()))
      return Response.error("Card Name must be supplied.", "embossedName", request.context());
    if (!request.embossedName().matches("[A-Za-z ]+"))
      return Response.error(
          "Card Name can have alphabets only.", "embossedName", request.context());
    if (blank(request.activeStatus()) || !request.activeStatus().matches("(?i)[YN]"))
      return Response.error("Card Status must be Y or N.", "activeStatus", request.context());
    if (blank(request.expirationDate()) || !request.expirationDate().matches("\\d{2}/\\d{2}"))
      return Response.error("Expiration Date is not valid", "expirationDate", request.context());
    if (request.preImage() == null
        || !Objects.equals(card.getEmbossedName(), request.preImage().embossedName())
        || !Objects.equals(card.getActiveStatus(), request.preImage().activeStatus())
        || !Objects.equals(card.getExpiraionDate(), request.preImage().expirationDate())) {
      return Response.error(
          "Record changed by some one else. Please review", "cardNumber", request.context());
    }
    if (Objects.equals(card.getEmbossedName(), request.embossedName())
        && Objects.equals(card.getActiveStatus(), request.activeStatus())
        && Objects.equals(card.getExpiraionDate(), request.expirationDate())) {
      return Response.error(
          "No change detected with respect to values fetched.", "cardNumber", request.context());
    }
    if (!request.confirm())
      return Response.ok(card, "COCRDUPC", withCard(request.context(), cardNumber, "COCRDUPC"));
    card.setEmbossedName(request.embossedName());
    card.setActiveStatus(request.activeStatus().toUpperCase());
    card.setExpiraionDate(request.expirationDate());
    return Response.ok(
        cards.save(card), "COCRDUPC", withCard(request.context(), cardNumber, "COCRDUPC"));
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  private static Context withCard(Context c, String card, String program) {
    return new Context(
        c == null ? "" : c.toProgram(),
        program,
        c == null ? "" : c.fromTransaction(),
        "CCRD",
        c == null ? "" : c.userId(),
        c == null ? "U" : c.userType(),
        c == null ? null : c.accountId(),
        card);
  }

  public record CardListRequest(
      Long accountId, String cardNumber, int page, String direction, Context context) {
    public CardListRequest(Long accountId, String cardNumber, int page, Context context) {
      this(accountId, cardNumber, page, "FORWARD", context);
    }
  }

  public record CardPage(
      List<Card> cards, int page, int pageSize, boolean nextPage, boolean previousPage) {}

  public record CardPreImage(String embossedName, String activeStatus, String expirationDate) {}

  public record CardUpdateRequest(
      String embossedName,
      String activeStatus,
      String expirationDate,
      CardPreImage preImage,
      boolean confirm,
      Context context) {}
}
