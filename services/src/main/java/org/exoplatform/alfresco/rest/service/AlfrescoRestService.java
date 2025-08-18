package org.exoplatform.alfresco.rest.service;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.exoplatform.alfresco.rest.api.AlfrescoServiceImpl;
import org.exoplatform.alfresco.rest.model.ServiceResponse;
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
import java.util.*;

@Path("/alfresco/documents")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 50,
        maxRequestSize = 1024 * 1024 * 100
)
public class AlfrescoRestService implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(AlfrescoRestService.class);
    private final AlfrescoServiceImpl service;

    private static final String TICKET = "ticket";
    private static final String FAILED_RESPONSE_CODE = "alfrescoFailedCode";
    private static final String FAILED_RESPONSE_MESSAGE = "alfrescoFailedMessage";
    private static final String FAILED_TITLE = "title";
    private static final String TICKET_NOT_FOUND = "No ticket found";
    private static final String UPLOAD_ERROR = "Upload Error";

    public AlfrescoRestService(AlfrescoServiceImpl service){
        this.service = service;
    }

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
                    .entity(Constants.USERNAME_OR_PASSWORD_ERROR).build();
        }
        try{
            ServiceResponse response = service.login(alfrescoUsername, alfrescoPass);
            if (response.getResponse() != null) {
                JSONObject jsonObject = (JSONObject) response.getResponse();
                request.getSession(true).setAttribute(TICKET, jsonObject.get(TICKET));
                return Response.ok(jsonObject.toString()).build();
            }else{
                LOG.error(Constants.LOGIN_FAIL, response.getStatus());
                return Response.status(response.getStatus())
                        .entity(response.getMessage())
                        .build();
            }
        } catch (Exception e) {
            LOG.error(Constants.LOGIN_FAILED_EXCEPTION, e);
            return Response.serverError().entity(Constants.LOGIN_FAILED_INTERNAL_ERROR).build();
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
                LOG.info(Constants.TICKET_REMOVED);
            } else {
                LOG.info(Constants.LOGOUT_REQUESTED);
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
        String token = getTicket(request);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
        }

        try {
            ServiceResponse response = service.getFiles(token);
            if (response.getResponse() != null) {
                List<Map<String, Object>> files = (List<Map<String, Object>>) response.getResponse();
                return Response.ok(files).build();
            }else{
                LOG.error(response.getMessage());
                return Response.status(response.getStatus())
                        .entity(response.getMessage())
                        .build();
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
                    .entity(createErrorResponse(UPLOAD_ERROR, "parent folder Id is missing.", 400))
                    .build();
        }

        String token = getTicket(request);
        if (token == null) {
            LOG.warn("Attempt to upload file without an Alfresco ticket.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
        }

        try {
            ServiceResponse response = service.uploadFile(request, token, parentNodeId);
            if (response.getResponse() != null) {
                Map<String, Object> newFileEntry = (Map<String, Object>) response.getResponse();
                return Response.ok(newFileEntry).build();
            }else{
                LOG.error(Constants.LOGIN_FAIL, response.getStatus());
                return Response.status(response.getStatus())
                        .entity(response.getMessage())
                        .build();
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

        String token = getTicket(request);
        if (token == null) {
            LOG.warn("Attempt to download file '{}' without an Alfresco ticket.", fileID);
            return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
        }

        try {
            ServiceResponse response = service.downloadFile(token, fileID);
            if (response.getResponse() != null) {
                byte[] newFileEntry = (byte[]) response.getResponse();
                return Response.ok(newFileEntry)
                        .header("Content-Disposition", "attachment; filename=\"" + fileID + "\"")
                        .build();
            }else{
                LOG.error(Constants.LOGIN_FAIL, response.getStatus());
                return Response.status(response.getStatus())
                        .entity(response.getMessage())
                        .build();
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

    private String createErrorResponse(String title, String message, int statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, title);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, message);
        jsonObject.put(FAILED_RESPONSE_CODE, statusCode);
        return jsonObject.toString();
    }
}