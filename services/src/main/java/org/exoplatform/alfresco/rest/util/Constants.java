package org.exoplatform.alfresco.rest.util;


/**
 * Application-wide string constants for Alfresco REST API URLs and messages.
 */
public final class Constants {

    /** Base URL of the Alfresco server. */
    public static final String ALFRESCO_URL =
            "http://http://192.168.1.199/:8080";

    /** Alfresco authentication endpoint (ticket creation). */
    public static final String ALFRESCO_API_LOGIN_URL =
            ALFRESCO_URL + "/alfresco/api/-default-/public/authentication/versions/1/tickets";

    /** Alfresco endpoint to list a node's children. */
    public static final String ALFRESCO_API_FILES_URL =
            ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";

    /** Alfresco endpoint to upload a file under a node. */
    public static final String ALFRESCO_API_UPLOAD_URL =
            ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";

    /** Alfresco endpoint to download a node's binary content. */
    public static final String ALFRESCO_API_DOWNLOAD_URL =
            ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content";

    // ──────────────────────────────────────────────── Error messages

    /** Error returned when username or password is missing in a login request. */
    public static final String USERNAME_OR_PASSWORD_ERROR =
            "Username and password must not be empty.";

    /** Generic login failure message (no format args). */
    public static final String LOGIN_FAILED = "Login failed with status.";

    /** Login failure message template accepting an HTTP status code argument. */
    public static final String LOGIN_FAIL = "Login failed with status %s.";

    /** Login failure message template accepting an exception message argument. */
    public static final String LOGIN_FAILED_EXCEPTION = "Login failed with exception: %s.";

    /** Login failure message for unexpected internal errors. */
    public static final String LOGIN_FAILED_INTERNAL_ERROR = "Login failed due to internal error.";

    /** Log message emitted when the Alfresco ticket is removed from the session. */
    public static final String TICKET_REMOVED = "Alfresco ticket removed from session.";

    /** Log message emitted when logout is requested but no active session exists. */
    public static final String LOGOUT_REQUESTED = "Logout requested, but no active session found.";

    // ──────────────────────────────────────────────── Success messages

    /** Success message for login operations. */
    public static final String LOGIN_SUCCESS = "Login successful.";

    /** Success message template for file upload, accepting the new node ID as argument. */
    public static final String FILE_UPLOADED_SUCCESSFULLY =
            "File uploaded successfully with nodeId: %s.";

    /** Success message template for file download, accepting the node ID as argument. */
    public static final String FILE_DOWNLOADED_SUCCESSFULLY =
            "File downloaded successfully with nodeId: %s.";

    /** Success message for listing a user's files. */
    public static final String FILES_FETCHED_SUCCESSFULLY = "Getting user files successfully";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Constants() {
    }
}
