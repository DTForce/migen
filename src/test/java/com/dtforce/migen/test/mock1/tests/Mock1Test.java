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

package com.dtforce.migen.test.mock1.tests;

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
import com.dtforce.migen.test.mock1.spring.Mock1;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dtforce.migen.test.TestTools.filterAddedTablesNames;
import static com.dtforce.migen.test.TestTools.tableColumns;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"migrationDiff", "disableMigrationCommand"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Mock1.class})
@TestPropertySource(properties = {
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
public class Mock1Test
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
			"contract",
			"contract_property",
			"contract_type",
			"contract_type_property",
			"contract_aud",
			"contract_type_aud",
			"contract_property_aud",
			"revinfo"
		);
		assertThat(changes).hasSize(15);
	}

	@Test
	public void testContractColumns() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		Map<String, AddTableChange> tableChangeMap = filterAddedTablesNames(changes);
		assertThat(tableChangeMap).containsKeys(
			"contract"
		);
		AddTableChange tableChange = tableChangeMap.get("contract");
		Table newTable = tableChange.getNewTable();
		Map<String, Column> tableColumns = tableColumns(newTable);

		assertThat(tableColumns).containsOnlyKeys(
			"id",
			"name",
			"note",
			"total",
			"type_name"
		);

		assertColumnType(tableColumns.get("id"), "UUID");
		assertColumnType(tableColumns.get("name"), "VARCHAR(255)");
		assertColumnType(tableColumns.get("total"), "NUMERIC(38,2)");
		assertColumnType(tableColumns.get("note"), "TEXT");
		assertColumnType(tableColumns.get("type_name"), "VARCHAR(255)");
	}

	@Test
	public void testContractPropertyColumns() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		Map<String, AddTableChange> tableChangeMap = filterAddedTablesNames(changes);
		assertThat(tableChangeMap).containsKeys(
			"contract"
		);
		AddTableChange tableChange = tableChangeMap.get("contract_property");
		Table newTable = tableChange.getNewTable();
		LinkedHashMap<String, Column> tableColumns = tableColumns(newTable);

		assertThat(new ArrayList<>(tableColumns.keySet())).containsExactly(
			"id_index_str",
			"bool_val",
			"date_val",
			"nbr_val",
			"str_val",
			"contract_id",
			"property_name",
			"type_name"
		);

		assertColumnType(tableColumns.get("id_index_str"), "VARCHAR(255)");
		assertColumnType(tableColumns.get("bool_val"), "BOOLEAN");
		assertColumnType(tableColumns.get("date_val"), "TIMESTAMP(6)");
		assertColumnType(tableColumns.get("nbr_val"), "NUMERIC(38,2)");
		assertColumnType(tableColumns.get("str_val"), "VARCHAR(255)");
		assertColumnType(tableColumns.get("contract_id"), "UUID");
		assertColumnType(tableColumns.get("property_name"), "VARCHAR(255)");
		assertColumnType(tableColumns.get("type_name"), "VARCHAR(255)");

		assertThat(newTable.getForeignKeyCount()).isEqualTo(2);
		assertThat(newTable.getForeignKey(0).getReferenceCount()).isEqualTo(1);
		assertThat(newTable.getForeignKey(0).getReference(0).getForeignColumnName()).isEqualTo("id");
		assertThat(newTable.getForeignKey(0).getReference(0).getLocalColumnName()).isEqualTo("contract_id");

		assertThat(newTable.getForeignKey(1).getReferenceCount()).isEqualTo(2);
		assertThat(newTable.getForeignKey(1).getReference(0).getForeignColumnName()).isEqualTo("property_name");
		assertThat(newTable.getForeignKey(1).getReference(0).getLocalColumnName()).isEqualTo("property_name");
		assertThat(newTable.getForeignKey(1).getReference(1).getForeignColumnName()).isEqualTo("type_name");
		assertThat(newTable.getForeignKey(1).getReference(1).getLocalColumnName()).isEqualTo("type_name");

	}

	private void assertColumnType(Column column, String type)
	{
		stringWriter.getBuffer().setLength(0);
		sqlBuilder.writeSqlColumnType(column);
		assertThat(stringWriter.toString()).isEqualTo(type);
	}


}
