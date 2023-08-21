package com.dtforce.migen.intergrations.spring;

import lombok.extern.slf4j.Slf4j;
import org.apache.ddlutils.Platform;
import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.dtforce.migen.MigrationGenerator;
import com.dtforce.migen.adapter.MetadataAdapter;
import com.dtforce.migen.adapter.hibernate.HibernateAdapter;
import com.dtforce.migen.adapter.hibernate.HibernateInfoHolder;
import com.dtforce.migen.platform.PlatformFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

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
