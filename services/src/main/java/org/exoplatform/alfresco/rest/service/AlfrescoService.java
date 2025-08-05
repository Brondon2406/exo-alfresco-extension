package org.exoplatform.alfresco.rest.service;

import java.io.InputStream;

public interface AlfrescoService {

    public String login(String username, String password);
    public String getFiles(String ticket);
    public String uploadFile(String ticket, String fileName, InputStream fileContent);
    public String downloadFile(String ticket, String fileId);
}
