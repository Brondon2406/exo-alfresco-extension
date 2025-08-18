package org.exoplatform.alfresco.rest.util;

public class Constants {
    public static final String ALFRESCO_URL = "http://ec2-user@ec2-3-15-3-141.us-east-2.compute.amazonaws.com:8080";
    public static final String ALFRESCO_API_LOGIN_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/authentication/versions/1/tickets";
    public static final String ALFRESCO_API_FILES_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    public static final String ALFRESCO_API_UPLOAD_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    public static final String ALFRESCO_API_DOWNLOAD_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content";

    // Error messages
    public static final String USERNAME_OR_PASSWORD_ERROR = "Username and password must not be empty.";
    public static final String LOGIN_FAILED = "Login failed with status.";
    public static final String LOGIN_FAIL = "Login failed with status %s.";
    public static final String LOGIN_FAILED_EXCEPTION = "Login failed with exception: %s.";
    public static final String LOGIN_FAILED_INTERNAL_ERROR = "Login failed due to internal error.";

    public static final String TICKET_REMOVED = "Alfresco ticket removed from session.";
    public static final String LOGOUT_REQUESTED = "Logout requested, but no active session found.";

    // Success messages
    public static final String LOGIN_SUCCESS = "Login successful.";
    public static final String FILE_UPLOADED_SUCCESSFULLY = "File uploaded successfully with nodeId: %s.";
    public static final String FILE_DOWNLOADED_SUCCESSFULLY = "File downloaded successfully with nodeId: %s.";
    public static final String FILES_FETCHED_SUCCESSFULLY = "Getting user files successfully";
} 