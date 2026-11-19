package tn.reclamationapp.dto;

public class ReclamationMessageRequest {

    private String message;

    public ReclamationMessageRequest() {
    }

    public ReclamationMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
