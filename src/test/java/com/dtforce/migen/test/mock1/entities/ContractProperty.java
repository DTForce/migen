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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@ToString(
	exclude = {"contract"}
)
@IdClass(ContractPropertyId.class)
@Entity
@Audited
public class ContractProperty
{

	@Id
	@Column
	private Contract contract;

	@Id
	@Column
	private String indexStr;

	@Id
	@Column
	private ContractTypeProperty property;

	@Column
	private String strVal;

	@Column
	private BigDecimal nbrVal;

	@Column
	private Boolean boolVal;

	@Column
	private Date dateVal;

	@Column
	private Instant instantVal;

}
