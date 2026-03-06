package org.exoplatform.alfresco.rest.service;


import jakarta.inject.Inject;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** File size threshold for multipart uploads: 1 MB. */
// NOTE: ces constantes doivent être déclarées AVANT l'annotation pour être utilisées dans @MultipartConfig
// En Java, les valeurs d'annotations doivent être des constantes de compilation (static final primitives).
// On les déclare dans un interface ou une classe utilitaire séparée, ou on accepte
// de les laisser inline dans l'annotation en les remplaçant par des constantes de classe séparée.
// La solution la plus propre pour @MultipartConfig est une classe de constantes dédiée.

@Path("/alfresco/documents")
@MultipartConfig(
        fileSizeThreshold = AlfrescoRestService.MULTIPART_THRESHOLD,
        maxFileSize       = AlfrescoRestService.MULTIPART_MAX_FILE,
        maxRequestSize    = AlfrescoRestService.MULTIPART_MAX_REQUEST
)
public final class AlfrescoRestService implements ResourceContainer {

    /** Multipart file size threshold in bytes (1 MB). */
    static final int MULTIPART_THRESHOLD = 1024 * 1024;

    /** Multipart maximum single file size in bytes (50 MB). */
    static final long MULTIPART_MAX_FILE = 1024L * 1024L * 50L;

    /** Multipart maximum total request size in bytes (100 MB). */
    static final long MULTIPART_MAX_REQUEST = 1024L * 1024L * 100L;

    /** Logger for this class. */
    private static final Log LOG = ExoLogger.getLogger(AlfrescoRestService.class);

    /** HTTP 400 Bad Request status code. */
    private static final int HTTP_BAD_REQUEST = 400;

    /** HTTP 500 Internal Server Error status code. */
    private static final int HTTP_INTERNAL_ERROR = 500;

    /** Session attribute key for the Alfresco authentication ticket. */
    private static final String TICKET = "ticket";

    /** Error response field key for the Alfresco status code. */
    private static final String FAILED_RESPONSE_CODE = "alfrescoFailedCode";

    /** Error response field key for the Alfresco error message. */
    private static final String FAILED_RESPONSE_MESSAGE = "alfrescoFailedMessage";

    /** Error response field key for the error title. */
    private static final String FAILED_TITLE = "title";

    /** Message returned when no ticket is found in the session. */
    private static final String TICKET_NOT_FOUND = "No ticket found";

    /** Error title used in upload error responses. */
    private static final String UPLOAD_ERROR = "Upload Error";

    /** The Alfresco service delegate. */
    private final AlfrescoServiceImpl service;

    /**
     * Constructs the REST service with the given Alfresco service implementation.
     *
     * @param alfrescoService the Alfresco service implementation to delegate to
     */
    @Inject
    public AlfrescoRestService(final AlfrescoServiceImpl alfrescoService) {  // FIX: HiddenField
        this.service = alfrescoService;
    }

    /**
     * Authenticates a user against Alfresco and stores the ticket in the HTTP session.
     *
     * @param request          the HTTP servlet request
     * @param alfrescoUsername the Alfresco username
     * @param alfrescoPass     the Alfresco password
     * @return 200 with ticket JSON, or an error response
     */
    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context final HttpServletRequest request,
                          @FormParam("alfrescoUsername") final String alfrescoUsername,
                          @FormParam("alfrescoPass") final String alfrescoPass) {

        if (alfrescoUsername == null || alfrescoUsername.isEmpty()
                || alfrescoPass == null || alfrescoPass.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Constants.USERNAME_OR_PASSWORD_ERROR).build();
        }
        try {
            ServiceResponse response = service.login(alfrescoUsername, alfrescoPass);
            if (response.getResponse() != null) {
                JSONObject jsonObject = (JSONObject) response.getResponse();
                request.getSession(true).setAttribute(TICKET, jsonObject.get(TICKET));
                return Response.ok(jsonObject.toString()).build();
            } else {                                               // FIX: WhitespaceAround
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

    /**
     * Logs out the current user by removing the Alfresco ticket from the session.
     *
     * @param request the HTTP servlet request
     * @return 200 with logged-out status, or an error response
     */
    @POST
    @Path("/logout")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context final HttpServletRequest request) {
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
                    .entity(createErrorResponse("Logout Error", "Internal error during logout.", HTTP_INTERNAL_ERROR))
                    .build();
        }
    }

    /**
     * Checks whether the current session contains a valid Alfresco ticket.
     *
     * @param request the HTTP servlet request
     * @return 200 with an {@code authenticated} boolean field
     */
    @GET
    @Path("/session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSession(@Context final HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute(TICKET) != null;
        LOG.info("Session check: isAuthenticated={}", isAuthenticated);
        return Response.ok(Collections.singletonMap("authenticated", isAuthenticated)).build();
    }

    /**
     * Returns the list of files from the authenticated user's Alfresco home folder.
     *
     * @param request the HTTP servlet request
     * @return 200 with file list JSON, 401 if not authenticated, or an error response
     */
    @GET
    @Path("/files")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiles(@Context final HttpServletRequest request) {
        String token = getTicket(request);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(TICKET_NOT_FOUND).build();
        }
        try {
            ServiceResponse response = service.getFiles(token);
            if (response.getResponse() != null) {
                List<Map<String, Object>> files = (List<Map<String, Object>>) response.getResponse();
                return Response.ok(files).build();
            } else {                                               // FIX: WhitespaceAround
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

    /**
     * Uploads a file to the specified Alfresco parent node.
     *
     * @param request      the HTTP servlet request (must contain a multipart "file" part)
     * @param parentNodeId the Alfresco node ID of the destination folder
     * @return 200 with the new file entry JSON, 400/401 on validation errors, or a server error
     */
    @POST
    @Path("/upload/{parentNodeId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Context final HttpServletRequest request,
                               @PathParam("parentNodeId") final String parentNodeId) {

        if (parentNodeId == null || parentNodeId.isEmpty()) {
            LOG.warn("Upload attempt with missing parent folder Id.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse(UPLOAD_ERROR, "parent folder Id is missing.", HTTP_BAD_REQUEST))
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
            } else {                                               // FIX: WhitespaceAround
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

    /**
     * Downloads a file from Alfresco by its node ID.
     *
     * @param request the HTTP servlet request
     * @param fileID  the Alfresco node ID of the file to download
     * @return 200 with the file bytes, 400/401 on validation errors, or a server error
     */
    @GET
    @Path("/download/{fileID}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @PermitAll
    public Response downloadFile(@Context final HttpServletRequest request,
                                 @PathParam("fileID") final String fileID) {

        if (fileID == null || fileID.isEmpty()) {
            LOG.warn("Download attempt with empty file ID.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorResponse("Download Error", "File ID must not be empty.", HTTP_BAD_REQUEST))
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
            } else {                                               // FIX: WhitespaceAround
                LOG.error(Constants.LOGIN_FAIL, response.getStatus());
                return Response.status(response.getStatus())
                        .entity(response.getMessage())
                        .build();
            }
        } catch (Exception e) {
            LOG.error("Download file exception: An unexpected error occurred for '{}'.", fileID, e);
            return Response.serverError()
                    .entity(createErrorResponse("Internal Server Error",
                            "Internal error during file download.", HTTP_INTERNAL_ERROR))
                    .build();
        }
    }

    /**
     * Retrieves the Alfresco authentication ticket from the current HTTP session.
     *
     * @param request the HTTP servlet request
     * @return the ticket string, or {@code null} if not present
     */
    private String getTicket(final HttpServletRequest request) {
        try {
            Object ticket = request.getSession().getAttribute(TICKET);
            return ticket != null ? ticket.toString() : null;
        } catch (Exception e) {
            LOG.error("Error retrieving ticket from session", e);
            return null;
        }
    }

    /**
     * Builds a JSON error response string.
     *
     * @param title      error title
     * @param message    error detail message
     * @param statusCode HTTP status code
     * @return JSON error string
     */
    private String createErrorResponse(final String title, final String message, final int statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FAILED_TITLE, title);
        jsonObject.put(FAILED_RESPONSE_MESSAGE, message);
        jsonObject.put(FAILED_RESPONSE_CODE, statusCode);
        return jsonObject.toString();
    }
}
