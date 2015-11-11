package com.github.anderskolsson.regserver.datastore;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.github.anderskolsson.regserver.datastore.datamodel.User;
import com.github.anderskolsson.regserver.exceptions.DataStoreException;
import com.github.anderskolsson.regserver.exceptions.UserCreationException;
import com.github.anderskolsson.regserver.exceptions.UserLookupException;

/**
 * Interface to the backing data store
 *
 */
public interface DataStore {
	/**
	 * Get a user by UUID.
	 * @param uuid the requested UUID
	 * @return the {@link User} corresponding to the UUID, {@value null} if user cannot be found
	 * @throws UserLookupException upon lookup failure, but not if the user simply doesn't exist
	 */
	public User getUser(UUID uuid) throws UserLookupException;
	
	/**
	 * Get a user by user name.
	 * @param uuid the requested user name
	 * @return the {@link User} corresponding to the user name, {@literal null} if user cannot be found
	 * @throws UserLookupException upon lookup failure, but not if the user simply doesn't exist.
	 */
	public User getUser(String userName) throws UserLookupException;
	
	/**
	 * Provision a new user in the {@link DataStore}.
	 * @param uuid a pre-generated UUID
	 * @param userName the required user ID, must be unique within this DataStore
	 * @param passwordHash the hash of the requested password
	 * @return the {@link User} Object
	 * @throws UserCreationException upon failure to provision the user
	 */
	public User createUser(UUID uuid, String userName, String passwordHash) throws UserCreationException;
	
	/**
	 * Get a list of the logins performed by this user, can only be done by the same authorized user.
	 * @param user the user to look for, and the credentials to use
	 * @param maxNum only get the N number of latest logs, 0 means no limit
	 * @return a {@link List} of the {@link Date}s the user has logged in. Dates are in UTC time zone
	 * @throws UserLookupException upon failure to find the user supplied
	 */
	public Date[] getAccessLog(User user, int maxNum) throws DataStoreException;
	
	/**
	 * Write DataStore record of one login by supplied user.
	 * @param user the user to register a login by
	 * @throws UserLookupException upon failure to find the user supplied
	 */
	public void registerLogin(User user) throws UserLookupException;
}
