package org.jboss.as.quickstarts.xa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * JPA Entity for storing key value pairs into a database.
 */
@SuppressWarnings("serial")
@Entity
public class Account implements Serializable {

	@Id
	@Column(unique = true, name = "name")
	private String name;

	@Column
	private Integer value;

	public Account() {
	}

	public Account(String name, Integer value) {
		setName(name);
		setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String toString() {
		return name + "=" + value;
	}
}
