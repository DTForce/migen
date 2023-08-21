package com.dtforce.migen.test.mock1.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Getter
@Setter
@ToString
@Entity
@Audited
public class ContractType
{
  @Id
  private String name;
}
