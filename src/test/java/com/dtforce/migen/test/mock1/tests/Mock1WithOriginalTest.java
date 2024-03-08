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
import com.dtforce.migen.ddl.ColumnDescriptionChanged;
import com.dtforce.migen.ddl.TableDescriptionChanged;
import com.dtforce.migen.platform.MigenPlatform;
import com.dtforce.migen.platform.MigenSqlBuilder;
import com.dtforce.migen.test.mock1.spring.Mock1;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

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
		assertThat(changes).hasSize(12);
	}

	@Test
	public void testComments() {
		List<ModelChange> changes = migrationGenerator.generateMigrationChanges();
		final var columnComments = changes.stream()
			.filter(it -> it instanceof ColumnDescriptionChanged)
			.map(it -> (ColumnDescriptionChanged) it)
			.collect(Collectors.toList());

		assertThat(columnComments).hasSize(1);
		assertThat(columnComments.get(0).getChangedColumn().getName()).isEqualTo("note");


		final var tableComments = changes.stream()
			.filter(it -> it instanceof TableDescriptionChanged)
			.map(it -> (TableDescriptionChanged) it)
			.collect(Collectors.toList());

		assertThat(tableComments).hasSize(1);
		assertThat(tableComments.get(0).getChangedTable().getName()).isEqualTo("contract");
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
