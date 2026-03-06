package org.exoplatform.alfresco.rest.model;


/**
 * Generic wrapper for responses returned by the Alfresco service layer.
 */
public final class ServiceResponse {

    /** The response payload object (JSON, byte array, list, etc.). */
    private Object response;

    /** The HTTP status code associated with this response. */
    private int status;

    /** A human-readable message describing the result. */
    private String message;

    /** No-arg constructor required for serialisation frameworks. */
    public ServiceResponse() { }

    /**
     * All-args constructor.
     *
     * @param responsePayload the response payload
     * @param statusCode      the HTTP status code
     * @param msg             the result message
     */
    public ServiceResponse(final Object responsePayload,
                           final int statusCode,
                           final String msg) {
        this.response = responsePayload;
        this.status = statusCode;
        this.message = msg;
    }

    /**
     * Returns a new {@link Builder} instance.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ServiceResponse}.
     */
    public static final class Builder {

        /** @see ServiceResponse#response */
        private Object response;

        /** @see ServiceResponse#status */
        private int status;

        /** @see ServiceResponse#message */
        private String message;

        /**
         * Sets the response payload.
         *
         * @param value the response payload
         * @return this builder
         */
        public Builder response(final Object value) {
            this.response = value;
            return this;
        }

        /**
         * Sets the HTTP status code.
         *
         * @param value the HTTP status code
         * @return this builder
         */
        public Builder status(final int value) {
            this.status = value;
            return this;
        }

        /**
         * Sets the result message.
         *
         * @param value the result message
         * @return this builder
         */
        public Builder message(final String value) {
            this.message = value;
            return this;
        }

        /**
         * Builds and returns the {@link ServiceResponse} instance.
         *
         * @return the constructed service response
         */
        public ServiceResponse build() {
            return new ServiceResponse(response, status, message);
        }
    }

    /**
     * Returns the response payload.
     *
     * @return the response object
     */
    public Object getResponse() {
        return response;
    }

    /**
     * Sets the response payload.
     *
     * @param value the new response object
     */
    public void setResponse(final Object value) {
        this.response = value;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param value the new status code
     */
    public void setStatus(final int value) {
        this.status = value;
    }

    /**
     * Returns the result message.
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the result message.
     *
     * @param value the new message
     */
    public void setMessage(final String value) {
        this.message = value;
    }
}
