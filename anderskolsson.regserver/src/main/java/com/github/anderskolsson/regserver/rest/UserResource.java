package com.github.anderskolsson.regserver.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.anderskolsson.regserver.authentication.Authentication;
import com.github.anderskolsson.regserver.datastore.DataStore;
import com.github.anderskolsson.regserver.datastore.datamodel.User;
import com.github.anderskolsson.regserver.exceptions.DataStoreException;
import com.github.anderskolsson.regserver.exceptions.UserCreationException;
import com.github.anderskolsson.regserver.exceptions.UserLookupException;
import com.github.anderskolsson.regserver.inputvalidation.InputDataValidator;
import com.github.anderskolsson.regserver.rest.elements.JsonUser;
import com.github.anderskolsson.regserver.rest.elements.LogRequest;
import com.github.anderskolsson.regserver.rest.elements.LogResponse;

/**
 * Handles REST requests for the "/users" path
 */
@Path("/users/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
	private final Logger logger;
	final DataStore store;
	final Authentication auth;
	private InputDataValidator validator;

	public UserResource(final DataStore store, final Authentication auth, final InputDataValidator validator) {
		this.store = store;
		this.auth = auth;
		this.validator = validator;
		this.logger = Logger.getLogger(UserResource.class.getName());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(final JsonUser user) {
		logger.log(Level.INFO, "Got user creation request for user: " + user);
		if (!validator.checkPasswordStrength(user.password)) {
			return Response.serverError().entity("Password not accepted").type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		if (!validator.checkUsername(user.userName)) {
			return Response.serverError().entity("Username not accepted").type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		try {
			UUID uuid = UUID.randomUUID();
			String hash = auth.hashPassword(user.password);
			User createdUser = store.createUser(uuid, user.userName, hash);
			logger.log(Level.INFO, "Created user: " + createdUser);
			return Response.created(new URI(createdUser.uuid.toString())).type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (UserCreationException | URISyntaxException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{uuid: [\\w-]+}")
	public Response getUser(@PathParam("uuid") final String uuidStr) {
		logger.log(Level.INFO, "Got get user request for uuid: " + uuidStr);
		try {
			User user = store.getUser(UUID.fromString(uuidStr));
			if (null == user) {
				return Response.serverError().entity("User not found").type(MediaType.TEXT_PLAIN_TYPE).build();
			}
			return Response.ok(user, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (UserLookupException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("login")
	public Response login(final JsonUser user) {
		logger.info("Recieved login request for user: " + user);
		User storedUser;
		try {
			if (null != user.uuid) {
				storedUser = store.getUser(UUID.fromString(user.uuid));
			} else if (null != user.userName) {
				storedUser = store.getUser(user.userName);
			} else {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Either UUID or username needs to be set")
						.build();
			}
			logger.log(Level.INFO, "User record for UUID " + user.uuid + ": " + storedUser);
			if (null == storedUser || !auth.verifyHash(user.password, storedUser.passwordHash)) {
				return Response.status(Status.UNAUTHORIZED)
						.entity("No such user :[" + user.uuid + "]: "+ user.userName+", or wrong password").build();
			}
			store.registerLogin(storedUser);
			return Response.ok("Login accepted").build();
		} catch (DataStoreException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getlogins")
	public Response getLogins(final LogRequest logRequest) {
		logger.info("Recieved login request for user: " + logRequest.credentials.toString());
		User storedUser;
		try {
			if (null != logRequest.credentials.uuid) {
				storedUser = store.getUser(UUID.fromString(logRequest.credentials.uuid));
			} else if (null != logRequest.credentials.userName) {
				storedUser = store.getUser(logRequest.credentials.userName);
			} else {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Either UUID or username needs to be set")
						.type(MediaType.TEXT_PLAIN_TYPE).build();
			}
			logger.log(Level.INFO, "User record for UUID " + logRequest.credentials.uuid + ": " + storedUser);
			if (null == storedUser || !auth.verifyHash(logRequest.credentials.password, storedUser.passwordHash)) {
				return Response.status(Status.UNAUTHORIZED)
						.entity("No such user UUID: '" + logRequest.credentials.uuid + "', or wrong password")
						.type(MediaType.TEXT_PLAIN_TYPE).build();
			}
			Date[] logins = store.getAccessLog(storedUser, logRequest.numLogs);
			store.registerLogin(storedUser);
			LogResponse response = new LogResponse(storedUser.name, datesToStrings(logins));
			return Response.ok(response).build();
		} catch (DataStoreException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
	}

	private static String[] datesToStrings(final Date[] dates) {
		final List<String> retList = new ArrayList<String>(dates.length);
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
		for (Date date : dates) {
			retList.add(df.format(date));
		}
		return retList.toArray(new String[retList.size()]);
	}
}
