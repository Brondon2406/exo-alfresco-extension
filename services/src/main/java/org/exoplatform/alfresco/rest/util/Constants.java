package org.exoplatform.alfresco.rest.util;

public class Constants {
    public static final String ALFRESCO_URL = "http://ec2-user@ec2-3-145-188-61.us-east-2.compute.amazonaws.com:8080";
    public static final String ALFRESCO_API_LOGIN_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/authentication/versions/1/tickets";
    public static final String ALFRESCO_API_FILES_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    public static final String ALFRESCO_API_UPLOAD_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/children";
    public static final String ALFRESCO_API_DOWNLOAD_URL = ALFRESCO_URL + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content";
} 