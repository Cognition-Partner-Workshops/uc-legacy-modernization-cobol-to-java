package carddemo.batch;

import java.io.IOException;

/** A COBOL batch program: runs against a job-step context and yields RETURN-CODE. */
public interface BatchProgram {
    int run(BatchContext ctx) throws IOException;

    /** 9910-DISPLAY-IO-STATUS as written in the three programs. */
    static void displayIoStatus(BatchContext ctx, String status) {
        boolean numeric = status.chars().allMatch(Character::isDigit);
        if (numeric && status.charAt(0) != '9') {
            ctx.display("FILE STATUS IS: NNNN00" + status);
        } else {
            int binary = status.charAt(1) & 0xFF;
            ctx.display("FILE STATUS IS: NNNN" + status.charAt(0) + String.format("%03d", binary));
        }
    }
}
