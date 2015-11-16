package com.github.anderskolsson.regserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

import com.github.anderskolsson.regserver.authentication.Authentication;
import com.github.anderskolsson.regserver.authentication.BcryptAuthentication;
import com.github.anderskolsson.regserver.datastore.DataStore;
import com.github.anderskolsson.regserver.datastore.DerbyDataStore;
import com.github.anderskolsson.regserver.inputvalidation.DefaultInputDataValidator;
import com.github.anderskolsson.regserver.inputvalidation.InputDataValidator;
import com.github.anderskolsson.regserver.rest.UserResource;

/**
 * Handles the initiation of the Server
 *
 */
public class RegServer {
	private static final String API_VERSION = "v1";
	private static final int PORT = 6789;
	private static Logger logger;
	public static void startServing(final Object ...objs) throws Exception{
		logger.severe("Starting Server");
		try {
			final ResourceConfig resourceConfig = new DefaultResourceConfig();
			resourceConfig.getSingletons().addAll(Arrays.asList(objs));
			final HttpServer server = GrizzlyServerFactory.createHttpServer("http://localhost:"+PORT+"/"+API_VERSION, resourceConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    server.stop();
                }
            }));
            server.start();
            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
        	logger.log(Level.SEVERE, null, ex);
        }
	}
	
	public static void main(final String args[]) throws Exception {
		logger =  Logger.getLogger(RegServer.class.getName());
		final Authentication auth = new BcryptAuthentication();
		final DataStore store = new DerbyDataStore("default", auth.getHashLength());
		final InputDataValidator validator = new DefaultInputDataValidator();
		final UserResource res = new UserResource(store, auth, validator);
		startServing(res);
	}
}
