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

package com.dtforce.migen.test.mock2.tests;

import org.apache.ddlutils.alteration.ModelChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dtforce.migen.MigrationGenerator;
import com.dtforce.migen.test.mock2.spring.Mock2;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"migrationDiff", "disableMigrationCommand"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Mock2.class})
@TestPropertySource(properties = {
	"spring.jpa.properties.hibernate.globally_quoted_identifiers = true", // IMPORTANT
	"spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions = true", // IMPORTANT
	"spring.jpa.hibernate.ddl-auto=none",
	"spring.sql.init.mode=always",
	"spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect",
	"spring.sql.init.platform=postgresql",
	"spring.sql.init.schema-locations=/sql/mock2/schema1.sql",
	"spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/postgres",
	"spring.datasource.username=postgres",
	"spring.datasource.password=postgres",
	"spring.datasource.driver-class-name=org.postgresql.Driver"
})
public class Mock2WithOriginalTest
{

	@Autowired
	private MigrationGenerator migrationGenerator;

	@Test
	public void testTables()
	{
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		assertThat(changes).hasSize(2);
	}

	@Test
	public void testComment() {
		String sql = migrationGenerator.generateMigrationSQL();
		assertThat(sql).contains("COMMENT ON COLUMN \"client.description\"\n    IS 'This is a description of the field description.';");
		assertThat(sql).contains("COMMENT ON TABLE \"client\"\n    IS 'This is a description of the table client.';");
	}
}
