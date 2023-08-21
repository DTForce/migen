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
