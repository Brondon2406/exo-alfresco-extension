package org.exoplatform.alfresco.rest.util;

import org.exoplatform.alfresco.rest.api.AlfrescoSettingImpl;
import org.exoplatform.alfresco.rest.model.AlfrescoSettingsEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Application-wide string constants for Alfresco REST API URLs and messages.
 */
public final class Constants {

    /** Logger for this class. */
    private static final Log LOG = ExoLogger.getLogger(Constants.class);

    /**
     * Alfresco settings
     */
    private final AlfrescoSettingImpl alfrescoSetting;

    /**
     * Constructor for IoC injection.
     *
     * @param alfrescoSetting the Alfresco setting service
     */
    public Constants(final AlfrescoSettingImpl alfrescoSetting) {
        this.alfrescoSetting = alfrescoSetting;
    }

    private String getAlfrescoUrl() {
        AlfrescoSettingsEntity setting = this.alfrescoSetting.getAlfrescoSettings();
        if (setting == null) {
            LOG.error("Alfresco setting is null");
            return null;
        }
        String url = setting.getServerApiUrl();
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Alfresco authentication endpoint (ticket creation). */
    public final String getAlfrescoApiUrl() {
        return getAlfrescoUrl() + "/alfresco/api/-default-/public/authentication/versions/1/tickets";
    }

    /** Alfresco endpoint to list a node's children. */
    public final String getAlfrescoApiFilesUrl(){
        return getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    }

    /** Alfresco endpoint to upload a file under a node. */
    public final String getAlfrescoApiUploadUrl(){
        return getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    }

    /** Alfresco endpoint to download a node's binary content. */
    public final String  getAlferscoApiDownloadUrl(){
        return getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content";
    }


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
}
