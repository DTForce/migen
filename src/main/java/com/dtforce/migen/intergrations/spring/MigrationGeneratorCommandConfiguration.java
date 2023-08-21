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

package com.dtforce.migen.intergrations.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.dtforce.migen.MigrationGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@Profile("migrationDiff & !disableMigrationCommand")
public class MigrationGeneratorCommandConfiguration
{

	@Bean
	CommandLineRunner generateMigration(final MigrationGenerator migrationGenerator) {
		return (args) -> {
			String description;

			if (args.length > 0) {
				description = args[0];
			} else if (System.console() != null) {
				description = System.console().readLine("Enter description for migration:");
			} else {
				description = "DESCRIPTION";
			}

			final String fileName = "V" +
				LocalDateTime.now().format(
					DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmm")
				) + "__" + description + ".sql";

			log.info("Generating migration {}.", fileName);

			final String migrationSQL = migrationGenerator.generateMigrationSQL();

			if (!migrationSQL.isEmpty()) {
				log.info("Migration sql generated {}.", migrationSQL);
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
				writer.write(migrationSQL);
				writer.close();
			} else {
				log.info("Schema is in sync.");
			}

			System.exit(0);
		};
	}

}
