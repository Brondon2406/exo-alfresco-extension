package org.exoplatform.alfresco.rest.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.alfresco.rest.api.AlfrescoSettingImpl;
import org.exoplatform.alfresco.rest.model.AlfrescoSettingsEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/alfresco/")
public class AlfrescoSettingsRestService implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(AlfrescoSettingsRestService.class);
    private final AlfrescoSettingImpl service;

    @Inject
    public AlfrescoSettingsRestService(final AlfrescoSettingImpl service){
        this.service = service;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("administrators")
    @Path("/settings/save")
    @Operation(summary = "Save Alfresco Settings", description = "Saves Alfresco Settings", method = "POST")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Object saved"),
        @ApiResponse(responseCode = "400", description = "Invalid query input"),
        @ApiResponse(responseCode = "500", description = "Internal server error"), })
    public Response saveAlfrescoSettings(@RequestBody(description = "AlfrescoSettings settings object", required = true)
                                     AlfrescoSettingsEntity alfrescoSettingsEntity) {
        if (alfrescoSettingsEntity == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("AlfrescoSettings object is mandatory").build();
        }
        try {
            AlfrescoSettingsEntity alfrescoSettings = service.saveAlfrescoSettings(alfrescoSettingsEntity.getServerApiUrl(),
                alfrescoSettingsEntity.getAppToken());
            return Response.status(Response.Status.CREATED).entity(alfrescoSettings).build();
        } catch (Exception e) {
            LOG.error("Error while saving Alfresco Settings", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users")
    @Path("/settings")
    @Operation(summary = "Retrieves Alfresco Settings", description = "Retrieves saved Alfresco Settings", method = "GET")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request fulfilled"),
        @ApiResponse(responseCode = "404", description = "Object not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error"), })
    public Response getAlfrescoSettings() {
        try {
            AlfrescoSettingsEntity settings = service.getAlfrescoSettings();
            if (settings == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(null)
                    .build();
            }
            return Response.ok(settings).build();
        } catch (Exception e) {
            LOG.error("Error while getting Alfresco Settings", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users")
    @Path("/settings/token")
    @Operation(summary = "Save Alfresco user token", description = "Save Alfresco user token", method = "POST")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Request fulfilled"),
        @ApiResponse(responseCode = "400", description = "Invalid query input"),
        @ApiResponse(responseCode = "401", description = "not authorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error"), })
    public Response saveAlfrescoUserToken(@Parameter(description = "Alfresco user token", required = true)
                                      String token) {
        if (StringUtils.isBlank(token)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Alfresco user token is mandatory").build();
        }
        Identity identity = ConversationState.getCurrent().getIdentity();
        try {
            if (!service.isUserTokenValid(token)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("token is not valid").build();
            }
            service.saveUserToken(token, identity.getUserId());
            return Response.noContent().build();
        } catch (Exception e) {
            LOG.error("Error while saving Alfresco user token", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users")
    @Path("/settings/token")
    @Operation(summary = "Remove Alfresco user token", description = "Remove Alfresco user token", method = "DELETE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Request fulfilled"),
        @ApiResponse(responseCode = "500", description = "Internal server error"),})
    public Response removeAlfrescoUserToken() {
        Identity identity = ConversationState.getCurrent().getIdentity();
        try {
            service.removeUserToken(identity.getUserId());
            return Response.noContent().build();
        } catch (Exception e) {
            LOG.error("Error while removing Alfresco user token", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
