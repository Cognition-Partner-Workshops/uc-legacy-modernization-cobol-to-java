package carddemo.programs;

import carddemo.JobContext;

/** A COBOL batch program: runs against a job context and returns the COBOL {@code RETURN-CODE}. */
public interface BatchProgram {
    int run(JobContext job);
}
