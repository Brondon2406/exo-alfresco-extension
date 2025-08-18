package org.exoplatform.alfresco.rest.api;

import jakarta.servlet.http.HttpServletRequest;
import org.exoplatform.alfresco.rest.model.ServiceResponse;

public interface AlfrescoService {

    public ServiceResponse login (String alfrescoUsername, String alfrescoPass);
    public ServiceResponse getFiles (String ticket);
    public ServiceResponse uploadFile(HttpServletRequest request, String ticket, String parentNodeId);
    public ServiceResponse downloadFile(String ticket, String fileID);
}
