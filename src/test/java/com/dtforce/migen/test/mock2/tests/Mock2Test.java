package com.dtforce.migen.test.mock2.tests;

import org.apache.ddlutils.alteration.AddForeignKeyChange;
import org.apache.ddlutils.alteration.AddTableChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
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
import com.dtforce.migen.test.mock2.spring.Mock2;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dtforce.migen.test.TestTools.filterAddedTablesNames;
import static com.dtforce.migen.test.TestTools.tableColumns;
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
	"spring.sql.init.schema-locations=/sql/clear.sql",
	"spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/postgres",
	"spring.datasource.username=postgres",
	"spring.datasource.password=postgres",
	"spring.datasource.driver-class-name=org.postgresql.Driver"
})
public class Mock2Test
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
			"client",
			"client_authorities"
		);
		assertThat(changes).hasSize(3);
	}

	@Test
	public void testForeignKeyQuotes() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		List<AddForeignKeyChange> changesFiltered = changes.stream()
			.filter(it -> it instanceof AddForeignKeyChange)
			.map(it -> (AddForeignKeyChange) it)
			.collect(Collectors.toList());

		assertThat(changesFiltered).hasSize(1);
		assertThat(changesFiltered.get(0).getNewForeignKey().getName()).doesNotStartWith("\"");

		String sql = migrationGenerator.generateMigrationSQL();
		assertThat(sql).doesNotContain("\"\"");
	}

	@Test
	public void testLtree() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		Map<String, AddTableChange> tableChangeMap = filterAddedTablesNames(changes);
		assertThat(tableChangeMap).containsKeys(
			"client"
		);
		AddTableChange tableChange = tableChangeMap.get("client");
		Table newTable = tableChange.getNewTable();
		LinkedHashMap<String, Column> tableColumns = tableColumns(newTable);

		assertThat(new ArrayList<>(tableColumns.keySet())).containsExactly(
			"id",
			"path"
		);

		assertColumnType(tableColumns.get("id"), "VARCHAR(255)");
		assertColumnType(tableColumns.get("path"), "LTREE");
	}

	private void assertColumnType(Column column, String type)
	{
		stringWriter.getBuffer().setLength(0);
		sqlBuilder.writeSqlColumnType(column);
		assertThat(stringWriter.toString()).isEqualTo(type);
	}

}
