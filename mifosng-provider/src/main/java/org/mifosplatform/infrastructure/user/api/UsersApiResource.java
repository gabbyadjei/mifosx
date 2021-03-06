package org.mifosplatform.infrastructure.user.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.office.data.OfficeLookup;
import org.mifosplatform.infrastructure.office.service.OfficeReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.api.data.AppUserData;
import org.mifosplatform.infrastructure.user.command.UserCommand;
import org.mifosplatform.infrastructure.user.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/users")
@Component
@Scope("singleton")
public class UsersApiResource {

    private final String resourceNameForPermissions = "USER";

    private final PlatformSecurityContext context;
    private final AppUserReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public UsersApiResource(final PlatformSecurityContext context, final AppUserReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveUsers(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<AppUserData> users = this.readPlatformService.retrieveAllUsers();

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, users);
    }

    @GET
    @Path("{userId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveUser(@PathParam("userId") final Long userId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        AppUserData user = this.readPlatformService.retrieveUser(userId);
        if (template) {
            List<OfficeLookup> offices = new ArrayList<OfficeLookup>(this.officeReadPlatformService.retrieveAllOfficesForLookup());
            user = new AppUserData(user, offices);
        }

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, user);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newUserDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final AppUserData user = this.readPlatformService.retrieveNewUserDetails();

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, user);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createUser(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "CREATE_USER");
        context.authenticatedUser().validateHasPermissionTo("CREATE_USER", allowedPermissions);

        final UserCommand command = this.apiDataConversionService.convertApiRequestJsonToUserCommand(null, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "users", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @PUT
    @Path("{userId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateUser(@PathParam("userId") final Long userId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "UPDATE_USER");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_USER", allowedPermissions);

        final UserCommand command = this.apiDataConversionService.convertApiRequestJsonToUserCommand(null, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "users", userId,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @DELETE
    @Path("{userId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteUser(@PathParam("userId") final Long userId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "DELETE_USER");
        context.authenticatedUser().validateHasPermissionTo("DELETE_USER", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "users", userId, "{}");

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}