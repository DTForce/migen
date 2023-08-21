package com.dtforce.migen.test.mock1.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@AllArgsConstructor(
	staticName = "of"
)
@Getter
@EqualsAndHashCode
public class ContractPropertyId implements Serializable {

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "contract_id", referencedColumnName = "id"),
	})
	private Contract contract;

	@Column(name = "id_index_str")
	private String indexStr;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "type_name", referencedColumnName = "type_name"),
		@JoinColumn(name = "property_name", referencedColumnName = "property_name")
	})
	private ContractTypeProperty property;

	/**
	 * For hibernate
	 */
	ContractPropertyId() {
	}

}
