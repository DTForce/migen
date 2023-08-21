package com.dtforce.migen.adapter;

import org.apache.ddlutils.model.Database;

import java.util.Map;

public interface MetadataAdapter
{
	Database getSchemaDatabase();

	Map<String,String> getTypeMappping();
}
