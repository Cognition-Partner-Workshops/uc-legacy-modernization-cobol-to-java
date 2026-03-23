package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CVTRA04Y - Transaction Category Type (RECLN 60).
 *
 * Original COBOL layout:
 *   05 TRAN-CAT-KEY
 *     10 TRAN-TYPE-CD    PIC X(02)
 *     10 TRAN-CAT-CD     PIC 9(04)
 *   05 TRAN-CAT-TYPE-DESC PIC X(50)
 *   05 FILLER             PIC X(04)
 */
public class TransactionCategoryRecord {

    private String typeCd;
    private int catCd;
    private String description;

    public TransactionCategoryRecord() {}

    public TransactionCategoryRecord(String typeCd, int catCd, String description) {
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.description = description;
    }

    public String getKey() {
        return String.format("%2s%04d", typeCd, catCd);
    }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public int getCatCd() { return catCd; }
    public void setCatCd(int catCd) { this.catCd = catCd; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "TransactionCategoryRecord{typeCd='" + typeCd + '\'' +
               ", catCd=" + catCd +
               ", description='" + description + '\'' +
               '}';
    }
}
