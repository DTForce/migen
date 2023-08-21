package com.dtforce.migen.test.mock1.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.dtforce.migen.intergrations.spring.EnableMigrationGenerator;

@SpringBootApplication
@EntityScan({"com.dtforce.migen.test.mock1"})
@EnableMigrationGenerator
public class Mock1
{
    public static void main(String[] args){
		SpringApplication.run(Mock1.class);}
}
