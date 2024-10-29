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

package com.dtforce.migen.test.mock2.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import com.dtforce.migen.intergrations.spring.EnableMigrationGenerator;
import com.dtforce.migen.platform.postgres.CustomPostgresqlPlatform;
import com.dtforce.migen.platform.type.PlatformTypeMapping;

@SpringBootApplication
@EntityScan({"com.dtforce.migen.test.mock2"})
@EnableMigrationGenerator
public class Mock2
{
    public static void main(String[] args) {
		SpringApplication.run(Mock2.class);
	}

	@Bean
	public PlatformTypeMapping platformTypeMapping()
	{
		return CustomPostgresqlPlatform.DEFAULT_TYPE_MAPPING;
	}
}
