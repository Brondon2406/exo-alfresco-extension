package org.exoplatform.alfresco.rest.model;

public class ServiceResponse {

    private Object response;
    private int status;
    private String message;

    public ServiceResponse() {}

    public ServiceResponse(Object response, int status, String message) {
        this.message = message;
        this.response = response;
        this.status = status;
    }

    // Static method to access the Builder
    public static ServiceResponse.Builder builder() {
        return new ServiceResponse.Builder();
    }

    // Builder class
    public static class Builder {
        private Object response;
        private int status;
        private String message;

        public ServiceResponse.Builder response(Object response) {
            this.response = response;
            return this;
        }

        public ServiceResponse.Builder status(int status) {
            this.status = status;
            return this;
        }

        public ServiceResponse.Builder message(String message) {
            this.message = message;
            return this;
        }

        public ServiceResponse build() {
            return new ServiceResponse(response, status, message);
        }
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
