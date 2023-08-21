package com.dtforce.migen.platform;

import lombok.extern.slf4j.Slf4j;

import com.dtforce.migen.platform.postgres.CustomPostgreSqlPlatform;

import java.util.Map;

@Slf4j
public class PlatformFactory
{
	public final static PlatformFactory INSTANCE = new PlatformFactory();

	public MigenPlatform createPlatform(String databaseProductName, Map<String, String> platformTypeMapping)
	{
		// TODO support more platforms

		final MigenPlatform platform;
		if (databaseProductName.toLowerCase().startsWith("postgres")) {
			platform = new CustomPostgreSqlPlatform(platformTypeMapping);
		} else if (databaseProductName.toLowerCase().equals("h2")) {
			log.warn("Using PostgreSQL platform for H2");
			platform = new CustomPostgreSqlPlatform(platformTypeMapping);
		} else {
			log.error("Unknown platform {}.", databaseProductName);
			throw new RuntimeException("Unknown platform: " + databaseProductName);
		}

		return platform;
	}

}
