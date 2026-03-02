package org.exoplatform.alfresco.rest.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.exoplatform.alfresco.rest.model.ServiceResponse;
import org.exoplatform.alfresco.rest.util.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation du service d'interaction avec l'API REST Alfresco.
 */
public final class AlfrescoServiceImpl implements AlfrescoService {

    @SuppressWarnings("JavadocVariable")
    private static final Log LOG = ExoLogger.getLogger(AlfrescoServiceImpl.class);
    private final ObjectMapper mapper = new ObjectMapper();

    // ──────────────────────────────────────────────── Constantes HTTP
    private static final int HTTP_CREATED           = 201;
    private static final int HTTP_OK                = 200;
    private static final int HTTP_NO_CONTENT        = 204;
    private static final int HTTP_BAD_REQUEST       = 400;
    private static final int HTTP_INTERNAL_ERROR    = 500;

    // ──────────────────────────────────────────────── Constantes buffer & autres
    private static final int DEFAULT_BUFFER_SIZE    = 4096;

    // ──────────────────────────────────────────────── Constantes métier (déjà existantes)
    private static final String ALFRESCO_USERNAME   = "alfrescoUsername";
    private static final String TICKET              = "ticket";
    private static final String FAILED_RESPONSE_CODE   = "alfrescoFailedCode";
    private static final String FAILED_RESPONSE_MESSAGE = "alfrescoFailedMessage";
    private static final String FAILED_TITLE        = "title";
    private static final String BASIC_AUTH_PREFIX   = "Basic ";
    private static final String ENTRY               = "entry";
    private static final String ROLE_TICKET         = "ROLE_TICKET:";
    private static final String NODE_ID             = "{nodeId}";
    private static final String AUTHORIZATION       = "Authorization";
    private static final String UPLOAD_ERROR        = "Upload Error";

    @Override
    public ServiceResponse login(String alfrescoUsername, String alfrescoPass) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(Constants.ALFRESCO_API_LOGIN_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", alfrescoUsername);
            jsonBody.put("password", alfrescoPass);
            post.setEntity(new StringEntity(jsonBody.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                if (status == HTTP_OK || status == HTTP_CREATED) {
                    JsonNode node = mapper.readTree(response.getEntity().getContent());
                    String ticketResponse = node.path(ENTRY).path("id").asText();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(TICKET, ticketResponse);
                    jsonObject.put(ALFRESCO_USERNAME, alfrescoUsername);
                    return ServiceResponse.builder()
                            .response(jsonObject)
                            .status(status)
                            .message(Constants.LOGIN_SUCCESS)
                            .build();
                } else {
                    LOG.error(Constants.LOGIN_FAIL, status);
                    return ServiceResponse.builder()
                            .response(null)
                            .status(status)
                            .message(Constants.LOGIN_FAILED)
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error(Constants.LOGIN_FAILED_EXCEPTION, e);
            return ServiceResponse.builder()
                    .response(null)
                    .status(HTTP_BAD_REQUEST)
                    .message(String.format(Constants.LOGIN_FAILED_EXCEPTION, e.getMessage()))
                    .build();
        }
    }

    @Override
    public ServiceResponse getFiles(String ticket) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = Constants.ALFRESCO_API_FILES_URL.replace(NODE_ID, "-my-");
            HttpGet get = new HttpGet(url);
            get.setHeader(AUTHORIZATION, buildAuthHeader(ticket));

            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getStatusLine().getStatusCode();
                if (status == HTTP_OK) {
                    JsonNode root = mapper.readTree(response.getEntity().getContent());
                    List<Map<String, Object>> files = generateResponseJsonNode(root);
                    LOG.info("Fetched {} files from Alfresco.", files.size());
                    return ServiceResponse.builder()
                            .message(Constants.FILES_FETCHED_SUCCESSFULLY)
                            .status(status)
                            .response(files)
                            .build();

                } else {
                    LOG.error("Failed to fetch files. status: {}", status);
                    ServiceResponse.builder()
                            .message(String.format("Failed to fetch files. status: %s", status))
                            .status(status)
                            .response(null)
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Get files exception", e);
        }
        return ServiceResponse.builder()
                .message("Internal error")
                .status(HTTP_BAD_REQUEST)
                .response(null)
                .build();
    }

    @Override
    public ServiceResponse uploadFile(HttpServletRequest request, String ticket, String parentNodeId) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            Part filePart = null;
            for (Part part : request.getParts()) {
                if ("file".equals(part.getName())) {
                    filePart = part;
                    break;
                }
            }
            if (filePart == null) {
                LOG.warn("No 'file' part found in the request");
                return ServiceResponse.builder()
                        .response(null)
                        .status(HTTP_BAD_REQUEST)
                        .message(createErrorResponse(UPLOAD_ERROR, "Missing 'file' part", HTTP_BAD_REQUEST))
                        .build();
            }

            InputStream uploadedInput = filePart.getInputStream();
            if (uploadedInput == null) {
                LOG.warn("Upload attempt with empty file input stream.");
                return ServiceResponse.builder()
                        .response(null)
                        .status(HTTP_BAD_REQUEST)
                        .message(createErrorResponse(UPLOAD_ERROR, "File input stream is empty.", HTTP_BAD_REQUEST))
                        .build();
            }

            String fileName = filePart.getSubmittedFileName();
            String url = Constants.ALFRESCO_API_UPLOAD_URL.replace(NODE_ID, parentNodeId);
            HttpPost post = new HttpPost(url);
            post.setHeader(AUTHORIZATION, buildAuthHeader(ticket));

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("filedata", uploadedInput, ContentType.DEFAULT_BINARY, fileName);
            post.setEntity(builder.build());
            LOG.info("Uploading file '{}' to Alfresco parent node: {} via URL: {}", fileName, parentNodeId, url);

            try (CloseableHttpResponse response = client.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= HTTP_OK && status < 300) {
                    JsonNode root = mapper.readTree(response.getEntity().getContent());
                    Map<String, Object> newFileEntry = mapToFileEntry(root.path(ENTRY));
                    LOG.info("Uploaded file '{}' to Alfresco successfully. Node ID: {}", fileName, root.path(ENTRY).path("id").asText());
                    return ServiceResponse.builder()
                            .response(newFileEntry)
                            .message(String.format(Constants.FILE_UPLOADED_SUCCESSFULLY, newFileEntry.get("id")))
                            .status(status)
                            .build();
                } else {
                    LOG.error("Failed to upload file to Alfresco. Status: {}", status);
                    return ServiceResponse.builder()
                            .response(null)
                            .message(getFailedResponse(response))
                            .status(status)
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Upload file exception", e);
        }
        return ServiceResponse.builder()
                .message("Internal error during upload")
                .status(HTTP_BAD_REQUEST)
                .response(null)
                .build();
    }

    @Override
    public ServiceResponse downloadFile(String ticket, String fileID) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = Constants.ALFRESCO_API_DOWNLOAD_URL.replace(NODE_ID, fileID);
            HttpGet get = new HttpGet(url);
            get.setHeader(AUTHORIZATION, buildAuthHeader(ticket));
            LOG.info("Attempting to download file '{}' from Alfresco URL: {}", fileID, url);

            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getStatusLine().getStatusCode();
                String reasonPhrase = response.getStatusLine().getReasonPhrase();

                if (status == HTTP_OK) {
                    if (response.getEntity() == null) {
                        LOG.error("Alfresco download response entity is null for file: {}", fileID);
                        return ServiceResponse.builder()
                                .response(response)
                                .message(createErrorResponse("Download Error", "Alfresco did not return file content.",
                                        HTTP_NO_CONTENT))
                                .status(Response.Status.NO_CONTENT.getStatusCode())
                                .build();
                    }

                    byte[] fileBytes = getBytes(response);

                    LOG.info("Successfully downloaded file '{}' ({} bytes) from Alfresco.", fileID, fileBytes.length);
                    return ServiceResponse.builder()
                            .status(status)
                            .message(String.format(Constants.FILE_DOWNLOADED_SUCCESSFULLY, fileID))
                            .response(fileBytes)
                            .build();
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    LOG.error("Failed to download file '{}' from Alfresco. Status: {}, Response: {}",
                            fileID, status, responseBody);
                    return ServiceResponse.builder()
                            .response(null)
                            .message(createFailedResponse(status, reasonPhrase))
                            .status(status)
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Download file exception: An unexpected error occurred during file download for '{}'.", fileID,
                    e);
            return ServiceResponse.builder()
                    .status(HTTP_INTERNAL_ERROR)
                    .message(createErrorResponse("Internal Server Error", "Internal error during file download.", HTTP_INTERNAL_ERROR))
                    .response(null)
                    .build();
        }
    }

    private static byte @NonNull [] getBytes(CloseableHttpResponse response) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream contentStream = response.getEntity().getContent()) {
            byte[] data = new byte[DEFAULT_BUFFER_SIZE];
            int nRead;
            while ((nRead = contentStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        }
        return buffer.toByteArray();
    }

    private String buildAuthHeader(String ticket) {
        String credentials = ROLE_TICKET + ticket;
        return BASIC_AUTH_PREFIX + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private List<Map<String, Object>> generateResponseJsonNode(JsonNode root) {
        if (root == null)
            return new ArrayList<>();
        List<Map<String, Object>> files = new ArrayList<>();
        for (JsonNode entry : root.path("list").path("entries")) {
            files.add(mapToFileEntry(entry.path(ENTRY)));
        }
        return files;
    }

    private Map<String, Object> mapToFileEntry(JsonNode json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> file = new HashMap<>();
        file.put("id", json.path("id").asText());
        file.put("name", json.path("name").asText());
        file.put("createdAt", json.path("createdAt").asText());
        file.put("modifiedAt", json.path("modifiedAt").asText());
        return file;
    }

    private String createErrorResponse(String title, String message, int statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, title);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, message);
        jsonObject.put(FAILED_RESPONSE_CODE, statusCode);
        return jsonObject.toString();
    }

    private String getFailedResponse(HttpResponse response) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(FAILED_TITLE, "Upload failed");
            jsonObject.put(FAILED_RESPONSE_CODE, response.getStatusLine().getStatusCode());
            jsonObject.put(FAILED_RESPONSE_MESSAGE, response.getStatusLine().getReasonPhrase());
            return jsonObject.toString();
        } catch (Exception e) {
            LOG.error("Error parsing failed response", e);
            return FAILED_TITLE + ": Unable to parse error response";
        }
    }

    private String createFailedResponse(int alfrescoStatusCode, String alfrescoReasonPhrase) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, "Download failed from Alfresco");
        jsonObject.put(FAILED_RESPONSE_CODE, alfrescoStatusCode);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, alfrescoReasonPhrase);
        return jsonObject.toString();
    }
}