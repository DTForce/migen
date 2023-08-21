package com.dtforce.migen.test.mock1.tests;

import org.apache.ddlutils.alteration.ModelChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dtforce.migen.MigrationGenerator;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.MigenSqlBuilder;
import com.dtforce.migen.test.mock1.spring.Mock1;

import java.io.StringWriter;
import java.util.List;

import static com.dtforce.migen.test.TestTools.filterAddedTablesNames;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"migrationDiff", "disableMigrationCommand"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Mock1.class})
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=none",
	"spring.sql.init.mode=always",
	"spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect",
	"spring.sql.init.platform=postgresql",
	"spring.sql.init.schema-locations=/sql/mock1/schema1.sql",
	"spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/postgres",
	"spring.datasource.username=postgres",
	"spring.datasource.password=postgres",
	"spring.datasource.driver-class-name=org.postgresql.Driver"
})
public class Mock1WithOriginalTest
{

	@Autowired
	private MigrationGenerator migrationGenerator;

	@Autowired
	private MigenPlatform migenPlatform;

	private StringWriter stringWriter;

	private MigenSqlBuilder sqlBuilder;

	@BeforeEach
	public void before() {
		stringWriter = new StringWriter();
		sqlBuilder = migenPlatform.getSqlBuilder();
		sqlBuilder.setWriter(stringWriter);
	}

	@Test
	public void testTables() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		assertThat(filterAddedTablesNames(changes)).containsOnlyKeys(
			"contract_aud",
			"contract_type_aud",
			"contract_property_aud",
			"revinfo"
		);
		assertThat(changes).hasSize(9);
	}

	@Test
	public void testTablesSQL() {
		String sql = migrationGenerator.generateMigrationSQL();
		assertThat(sql).containsIgnoringCase(
			"CREATE INDEX \"FKdwmknd8t7wjko72bg4ka0gtnb_idx\" ON \"contract_aud\" (\"rev\");"
		);
		assertThat(sql).containsIgnoringCase(
			"CREATE INDEX \"FK1t1435ll03xji015t8ycxwt07_idx\" ON \"contract_property_aud\" (\"rev\");"
		);
		assertThat(sql).containsIgnoringCase(
			"CREATE INDEX \"FKgi8bf2wy6rmtm3x7vjwwns1qt_idx\" ON \"contract_type_aud\" (\"rev\");"
		);
	}

}
