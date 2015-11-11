package com.github.anderskolsson.regserver.datastore;

import com.github.anderskolsson.regserver.datastore.datamodel.User;

/**
 * Helper class for implementers of {@link DataStore}
 *
 */
abstract public class AbstractStore implements DataStore {
	boolean isValidUserObj(final User user){
		if(null != user){
			if(null != user.name && !user.name.isEmpty()){
				if(null != user.passwordHash && !user.passwordHash.isEmpty()){
					return true;
				}
			}
		}
		return false;
	}
}
