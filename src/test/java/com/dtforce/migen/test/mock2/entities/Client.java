package com.dtforce.migen.test.mock2.entities;

import com.dtforce.migen.adapter.hibernate.type.OtherStringType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.Set;

@Entity
@Data
public class Client
{
	@Id
	private String id;

	@ElementCollection
	private Set<String> authorities;

	@Column(name = "path", nullable = false, columnDefinition = "ltree")
	@Type(value = OtherStringType.class)
	private String path;
}
