package com.gc.history.entity;

import java.util.List;

public class HistoryResult {

    private int error_code;
    private String reason;
    private List<History> result;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<History> getResult() {
        return result;
    }

    public void setResult(List<History> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "HistoryResult{" +
                "error_code=" + error_code +
                ", reason='" + reason + '\'' +
                ", result=" + result +
                '}';
    }
}
