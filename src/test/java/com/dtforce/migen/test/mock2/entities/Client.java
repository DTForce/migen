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

package com.dtforce.migen.test.mock2.entities;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Type;

import com.dtforce.migen.adapter.hibernate.type.OtherStringType;

import java.util.Set;

@Entity
@Data
@Comment("This is a description of the table client.")
public class Client
{
	@Id
	private String id;

	@ElementCollection
	private Set<String> authorities;

	@Column(name = "path", nullable = false, unique = true, columnDefinition = "ltree")
	@Type(value = OtherStringType.class)
	private String path;

	@Column(name = "description", columnDefinition = "text")
	@Comment("This is a description of the field description.")
	private String description;
}
