package com.dtforce.migen.test.mock1.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Getter
@Setter
@ToString
@IdClass(ContractTypePropertyId.class)
@Entity
public class ContractTypeProperty
{

	@Id
	private String propertyName;

	@Id
	private ContractType type;

	@Column(name = "property_type")
	private String propertyType;

	@Column(name = "required")
	private boolean required;

	public void setId(ContractType type, String propertyName) {
		this.type = type;
		this.propertyName = propertyName;
	}

}
