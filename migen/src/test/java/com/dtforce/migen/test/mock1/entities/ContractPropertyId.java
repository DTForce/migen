/*
 *    Copyright 2023 Jan Mareš, DTForce s.r.o.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
