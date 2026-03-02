package org.exoplatform.alfresco.rest.api;

import jakarta.servlet.http.HttpServletRequest;
import org.exoplatform.alfresco.rest.model.ServiceResponse;

public interface AlfrescoService {

    ServiceResponse login (String alfrescoUsername, String alfrescoPass);
    ServiceResponse getFiles (String ticket);
    ServiceResponse uploadFile(HttpServletRequest request, String ticket, String parentNodeId);
    ServiceResponse downloadFile(String ticket, String fileID);
}
