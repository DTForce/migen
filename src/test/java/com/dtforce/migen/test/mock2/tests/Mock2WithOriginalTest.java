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
		assertThat(changes).hasSize(0);
	}

}
