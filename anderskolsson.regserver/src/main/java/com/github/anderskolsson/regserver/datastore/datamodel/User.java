package com.github.anderskolsson.regserver.datastore.datamodel;

import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.github.anderskolsson.regserver.datastore.DataStore;

/**
 * Representation of a User in the {@link DataStore}
 *
 */
@XmlRootElement
public class User {
	public UUID uuid;
	public String name;
	// This element is hidden when outputting this as JSON or XML
	@XmlTransient
	public String passwordHash;
	public User(){
		super();
	}
	public User(final UUID uuid, final String name, final String passwordHash) {
		this.uuid = uuid;
		this.name = name;
		this.passwordHash = passwordHash;
	}
	@Override
	public String toString() {
		return "["+uuid+"]: "+name;
	}
}
