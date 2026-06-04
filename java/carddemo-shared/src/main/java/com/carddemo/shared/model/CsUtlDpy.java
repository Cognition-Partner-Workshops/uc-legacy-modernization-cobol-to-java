package com.carddemo.shared.model;

/**
 * Java equivalent of the parameter fields used by the reusable date-edit
 * procedure copybook {@code CSUTLDPY} ({@code app/cpy/CSUTLDPY.cpy}).
 *
 * <p>{@code CSUTLDPY} is a {@code PROCEDURE DIVISION} copybook that performs the
 * {@code EDIT-DATE-CCYYMMDD} routine. It reads/writes a small set of shared
 * fields: the name of the variable being validated (used to build messages),
 * an "input error" flag, a switch controlling whether messages are produced,
 * and the return message itself. This mutable POJO captures those parameters so
 * the Java port can pass them in and read the results back out.</p>
 */
public class CsUtlDpy {

    /**
     * Controls whether {@code CSUTLDPY} builds a return message, mirroring the
     * {@code WS-RETURN-MSG-OFF}/{@code WS-RETURN-MSG-ON} 88-levels.
     */
    public enum ReturnMsgSwitch {
        /** {@code WS-RETURN-MSG-OFF} — messages are produced. */
        OFF,
        /** {@code WS-RETURN-MSG-ON} — message production suppressed. */
        ON
    }

    /** {@code WS-EDIT-VARIABLE-NAME} — field name used in generated messages. */
    private String editVariableName;

    /** {@code INPUT-ERROR} flag — set true when validation fails. */
    private boolean inputError;

    /** {@code WS-RETURN-MSG} — the generated validation message. */
    private String returnMsg;

    /** {@code WS-RETURN-MSG-OFF}/{@code WS-RETURN-MSG-ON} switch. */
    private ReturnMsgSwitch returnMsgSwitch = ReturnMsgSwitch.OFF;

    /** Creates an empty parameter block. */
    public CsUtlDpy() {
    }

    /**
     * Creates a parameter block for validating a named variable.
     *
     * @param editVariableName the field name used in generated messages
     */
    public CsUtlDpy(String editVariableName) {
        this.editVariableName = editVariableName;
    }

    public String getEditVariableName() {
        return editVariableName;
    }

    public void setEditVariableName(String editVariableName) {
        this.editVariableName = editVariableName;
    }

    public boolean isInputError() {
        return inputError;
    }

    public void setInputError(boolean inputError) {
        this.inputError = inputError;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public ReturnMsgSwitch getReturnMsgSwitch() {
        return returnMsgSwitch;
    }

    public void setReturnMsgSwitch(ReturnMsgSwitch returnMsgSwitch) {
        this.returnMsgSwitch = returnMsgSwitch;
    }

    /** @return {@code true} when message production is enabled (OFF switch). */
    public boolean isReturnMsgOff() {
        return returnMsgSwitch == ReturnMsgSwitch.OFF;
    }
}
