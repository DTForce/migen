package com.dtforce.migen.test.mock1.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@AllArgsConstructor(staticName = "of")
@Getter
@EqualsAndHashCode
public class ContractTypePropertyId implements Serializable
{

	@Column(name = "property_name")
	private String propertyName;

	@ManyToOne
	@JoinColumn(name = "type_name", referencedColumnName = "name")
	private ContractType type;

	/**
	 * For hibernate
	 */
	ContractTypePropertyId() {
	}

}
