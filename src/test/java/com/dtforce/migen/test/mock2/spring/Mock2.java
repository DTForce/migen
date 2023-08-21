package com.dtforce.migen.test.mock2.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.dtforce.migen.intergrations.spring.EnableMigrationGenerator;

@SpringBootApplication
@EntityScan({"com.dtforce.migen.test.mock2"})
@EnableMigrationGenerator
public class Mock2
{
    public static void main(String[] args) {
		SpringApplication.run(Mock2.class);
	}
}
