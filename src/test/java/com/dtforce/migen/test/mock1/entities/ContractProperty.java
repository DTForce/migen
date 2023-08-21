package com.dtforce.migen.test.mock1.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

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

}
