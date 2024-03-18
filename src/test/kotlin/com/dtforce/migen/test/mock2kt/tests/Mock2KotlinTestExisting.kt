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
package com.dtforce.migen.test.mock2kt.tests

import com.dtforce.dokka.json.DokkaJsonModule
import com.dtforce.migen.MigrationGenerator
import com.dtforce.migen.platform.MigenPlatform
import com.dtforce.migen.platform.MigenSqlBuilder
import com.dtforce.migen.test.mock2kt.spring.Mock2Kotlin
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.StringWriter

@ActiveProfiles("migrationDiff", "disableMigrationCommand")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [Mock2Kotlin::class])
@TestPropertySource(properties = [
    "spring.jpa.properties.hibernate.globally_quoted_identifiers = true", // IMPORTANT
    "spring.jpa.properties.hibernate.globally_quoted_identifiers_skip_column_definitions = true", // IMPORTANT
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect",
    "spring.sql.init.platform=postgresql",
    "spring.sql.init.schema-locations=/sql/mock2/schema1.sql",
    "spring.datasource.url=jdbc:postgresql://\${DB_HOST:localhost}:5432/postgres",
    "spring.datasource.username=postgres",
    "spring.datasource.password=postgres",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
])
class Mock2KotlinTestExisting {
    @Autowired
    private val migrationGenerator: MigrationGenerator? = null

    @Autowired
    private val migenPlatform: MigenPlatform? = null

    @Autowired
    private val dokkaJsonModule: DokkaJsonModule? = null

    private var stringWriter: StringWriter? = null

    private lateinit var sqlBuilder: MigenSqlBuilder

    @BeforeEach
    fun before() {
        stringWriter = StringWriter()
        sqlBuilder = migenPlatform!!.sqlBuilder
        sqlBuilder.setWriter(stringWriter)
    }

    @Test
    fun testComment() {
        val sql = migrationGenerator!!.generateMigrationSQL()
        Assertions.assertThat(sql).contains("COMMENT ON COLUMN \"client.description\"\n    IS 'This is a description of the field description.';")
        Assertions.assertThat(sql).contains("COMMENT ON COLUMN \"client.path\"\n    IS 'This is a description of the field path.';")
        Assertions.assertThat(sql).contains("COMMENT ON TABLE \"client_authorities\"\n    IS 'Comment of authorities.';")
        Assertions.assertThat(sql).contains("COMMENT ON TABLE \"client\"\n    IS 'Client entity.';")
    }
}
