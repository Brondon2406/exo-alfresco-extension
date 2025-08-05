package org.exoplatform.alfresco.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.exoplatform.alfresco.rest.model.Node;
import org.exoplatform.alfresco.rest.util.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.json.JSONObject;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Path("/alfresco/documents")
public class AlfrescoServiceImpl implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(AlfrescoServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String ALFRESCO_USERNAME = "alfrescoUsername";
    private static final String TICKET = "ticket";
    private static final String FAILED_RESPONSE_CODE = "alfrescoFailedCode";
    private static final String FAILED_RESPONSE_MESSAGE = "alfrescoFailedMessage";
    private static final String FAILED_TITLE = "title";
    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private static final String ENTRY = "entry";
    private static final String ROLE_TICKET = "ROLE_TICKET:";
    private static final String TICKET_NOT_FOUND = "No ticket found";
    private static final String NODE_ID = "{nodeId}";
    private static final String AUTHORIZATION = "Authorization";

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context HttpServletRequest request,
            @FormParam("alfrescoUsername") String alfrescoUsername,
            @FormParam("alfrescoPass") String alfrescoPass) {

        if (alfrescoUsername == null || alfrescoUsername.isEmpty() || alfrescoPass == null || alfrescoPass.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username and password must not be empty").build();
        }

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
                if (status == 201) {
                    JsonNode node = mapper.readTree(response.getEntity().getContent());
                    String ticket = node.path(ENTRY).path("id").asText();
                    request.getSession(true).setAttribute(TICKET, ticket);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(TICKET, ticket);
                    jsonObject.put(ALFRESCO_USERNAME, alfrescoUsername);
                    return Response.ok(jsonObject.toString()).build();
                } else {
                    LOG.error("Login failed with status: " + status);
                    return Response.status(status)
                            .entity(getFailedResponse(response, "Login to Alfresco failed"))
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Login exception", e);
            return Response.serverError().entity("Internal error during login").build();
        }
    }

    @POST
    @Path("/logout")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(TICKET);
                LOG.info("Alfresco ticket removed from session.");
            } else {
                LOG.info("Logout requested, but no active session found.");
            }
            return Response.ok(Collections.singletonMap("status", "logged out")).build();
        } catch (Exception e) {
            LOG.error("Logout exception", e);
            return Response.serverError()
                    .entity(createErrorResponse("Logout Error", "Internal error during logout.", 500))
                    .build();
        }
    }

    @GET
    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSession(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute(TICKET) != null;
        LOG.info("Session check: isAuthenticated={}", isAuthenticated);
        return Response.ok(Collections.singletonMap("authenticated", isAuthenticated)).build();
    }

    @GET
    @Path("/files")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiles(@Context HttpServletRequest request) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String token = getTicket(request);
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
            }

            String url = Constants.ALFRESCO_API_FILES_URL.replace(NODE_ID, "-my-");
            HttpGet get = new HttpGet(url);
            get.setHeader(AUTHORIZATION, buildAuthHeader(token));

            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    JsonNode root = mapper.readTree(response.getEntity().getContent());
                    List<Map<String, Object>> files = generateResponseJsonNode(root);
                    LOG.info("Fetched {} files from Alfresco.", files.size());
                    return Response.ok(files).build();

                } else {
                    LOG.error("Failed to fetch files. status: {}", status);
                    return Response.status(status)
                            .entity(Collections.singletonMap("error", "Failed to fetch files"))
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Get files exception", e);
            return Response.serverError().entity("Internal error").build();
        }
    }

    @POST
    @Path("/upload/{parentNodeId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Context HttpServletRequest request,
            @PathParam("parentNodeId") String parentNodeId) {

        if (parentNodeId == null || parentNodeId.isEmpty()) {
            LOG.warn("Upload attempt with missing parent folder Id.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse("Upload Error", "parent folder Id is missing.", 400))
                    .build();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String token = getTicket(request);
            if (token == null) {
                LOG.warn("Attempt to upload file without an Alfresco ticket.");
                return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
            }

            Collection<Part> parts = request.getParts();
            LOG.info("Parts reçus : {}", parts.stream().map(Part::getName).collect(Collectors.toList()));

            Part filePart = request.getPart("file");
            if (filePart == null) {
                LOG.warn("No 'file' part found in the request");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Upload Error", "Missing 'file' part", 400))
                        .build();
            }

            InputStream uploadedInput = filePart.getInputStream();
            if (uploadedInput == null) {
                LOG.warn("Upload attempt with empty file input stream.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Upload Error", "File input stream is empty.", 400))
                        .build();
            }

            String fileName = filePart.getSubmittedFileName();
            String url = Constants.ALFRESCO_API_UPLOAD_URL.replace(NODE_ID, parentNodeId);
            HttpPost post = new HttpPost(url);
            post.setHeader(AUTHORIZATION, buildAuthHeader(token));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("filedata", uploadedInput, ContentType.DEFAULT_BINARY, fileName);
            post.setEntity(builder.build());
            LOG.info("Uploading file '{}' to Alfresco parent node: {} via URL: {}", fileName, parentNodeId, url);

            try (CloseableHttpResponse response = client.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                if (status == 201) {
                    JsonNode root = mapper.readTree(response.getEntity().getContent());
                    List<Map<String, Object>> files = generateResponseJsonNode(root);
                    LOG.info("Uploaded file '{}' to Alfresco under parent node '{}'.", fileName, parentNodeId);
                    LOG.info("Fetched {} files from Alfresco after upload.", files.size());
                    return Response.ok(files).build();

                } else {
                    LOG.error("Failed to fetch files. status: {}", status);
                    return Response.status(response.getStatusLine()
                            .getStatusCode())
                            .entity(getFailedResponse(response, "Upload failed")).build();
                }
            }
        } catch (Exception e) {
            LOG.error("Upload file exception", e);
            return Response.serverError().entity("Internal error during upload").build();
        }
    }

    @GET
    @Path("/download/{fileID}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @PermitAll
    public Response downloadFile(@Context HttpServletRequest request,
            @PathParam("fileID") String fileID) {

        if (fileID == null || fileID.isEmpty()) {
            LOG.warn("Download attempt with empty file ID.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse("Download Error", "File ID must not be empty.", 400))
                    .build();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String token = getTicket(request);
            if (token == null) {
                LOG.warn("Attempt to download file '{}' without an Alfresco ticket.", fileID);
                return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
            }

            String url = Constants.ALFRESCO_API_DOWNLOAD_URL.replace(NODE_ID, fileID);
            HttpGet get = new HttpGet(url);
            get.setHeader(AUTHORIZATION, buildAuthHeader(token));
            LOG.info("Attempting to download file '{}' from Alfresco URL: {}", fileID, url);

            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getStatusLine().getStatusCode();
                String reasonPhrase = response.getStatusLine().getReasonPhrase();

                if (status == 200) {
                    if (response.getEntity() == null) {
                        LOG.error("Alfresco download response entity is null for file: {}", fileID);
                        return Response.status(Response.Status.NO_CONTENT)
                                .entity(createErrorResponse("Download Error", "Alfresco did not return file content.",
                                        204))
                                .build();
                    }

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    try (InputStream contentStream = response.getEntity().getContent()) {
                        byte[] data = new byte[4096];
                        int nRead;
                        while ((nRead = contentStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                    }
                    byte[] fileBytes = buffer.toByteArray();

                    LOG.info("Successfully downloaded file '{}' ({} bytes) from Alfresco.", fileID, fileBytes.length);
                    return Response.ok(fileBytes)
                            .header("Content-Disposition", "attachment; filename=\"" + fileID + "\"")
                            .build();
                } else {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    LOG.error("Failed to download file '{}' from Alfresco. Status: {}, Response: {}",
                            fileID, status, responseBody);
                    return Response.status(status)
                            .entity(createFailedResponse(status, reasonPhrase, "Download failed from Alfresco"))
                            .build();
                }
            }
        } catch (Exception e) {
            LOG.error("Download file exception: An unexpected error occurred during file download for '{}'.", fileID,
                    e);
            return Response.serverError()
                    .entity(createErrorResponse("Internal Server Error", "Internal error during file download.", 500))
                    .build();
        }
    }

    private String getTicket(HttpServletRequest request) {
        try {
            Object ticket = request.getSession().getAttribute(TICKET);
            return ticket != null ? ticket.toString() : null;
        } catch (Exception e) {
            LOG.error("Error retrieving ticket from session", e);
            return null;
        }
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
            Node node = mapToNode(entry.path(ENTRY));

            Map<String, Object> file = new HashMap<>();
            file.put("id", node.getId());
            file.put("name", node.getName());
            file.put("createdAt", node.getCreatedAt());
            file.put("modifiedAt", node.getModifiedAt());

            files.add(file);
        }
        return files;
    }

    private Node mapToNode(JsonNode json) {
        return Node.builder()
                .id(json.path("id").asText())
                .name(json.path("name").asText())
                .nodeType(json.path("nodeType").asText())
                .isFolder(json.path("isFolder").asBoolean())
                .isFile(json.path("isFile").asBoolean())
                .createdAt(json.path("createdAt").asText())
                .modifiedAt(json.path("modifiedAt").asText())
                .parentId(json.path("parentId").asText())
                .isLink(json.path("isLink").asBoolean(false))
                .isFavorite(json.path("isFavorite").asBoolean(false))
                .isDirectLinkEnabled(json.path("isDirectLinkEnabled").asBoolean(false))
                .build();
    }

    private String getFailedResponse(HttpResponse response, String title) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(FAILED_TITLE, title);
            jsonObject.put(FAILED_RESPONSE_CODE, response.getStatusLine().getStatusCode());
            jsonObject.put(FAILED_RESPONSE_MESSAGE, response.getStatusLine().getReasonPhrase());
            return jsonObject.toString();
        } catch (Exception e) {
            LOG.error("Error parsing failed response", e);
            return FAILED_TITLE + ": Unable to parse error response";
        }
    }

    private String createErrorResponse(String title, String message, int statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, title);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, message);
        jsonObject.put(FAILED_RESPONSE_CODE, statusCode);
        return jsonObject.toString();
    }

    private String createFailedResponse(int alfrescoStatusCode, String alfrescoReasonPhrase, String title) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, title);
        jsonObject.put(FAILED_RESPONSE_CODE, alfrescoStatusCode);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, alfrescoReasonPhrase);
        return jsonObject.toString();
    }
}
