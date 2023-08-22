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

import com.dtforce.migen.ddl.RawTypedColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PlatformTypeMapping {

	private final Map<String, Function<RawTypedColumn, RawTypedColumn>> processors;

	private final List<BiFunction<String, RawTypedColumn, RawTypedColumn>> generalProcessors;

	public PlatformTypeMapping()
	{
		processors = new HashMap<>();
		generalProcessors = new ArrayList<>();
		generalProcessors.add((dbType, rawTypedColumn) -> {
			rawTypedColumn.setRawCompleteType(dbType);
			return rawTypedColumn;
		});
	}

	public RawTypedColumn map(String dbType, RawTypedColumn column) {
		RawTypedColumn columnMapped = column;
		for (BiFunction<String, RawTypedColumn, RawTypedColumn> generalProcessor : generalProcessors) {
			columnMapped = generalProcessor.apply(dbType, columnMapped);
		}
		if (processors.containsKey(columnMapped.getRawCompleteType())) {
			columnMapped = processors.get(columnMapped.getRawCompleteType()).apply(columnMapped);
		}
		return columnMapped;
	}

	public PlatformTypeMapping withMapping(String type, Function<RawTypedColumn, RawTypedColumn> processor) {
		processors.put(type, processor);
		return this;
	}

	public PlatformTypeMapping withGeneralProcessor(BiFunction<String, RawTypedColumn, RawTypedColumn> processor) {
		generalProcessors.add(processor);
		return this;
	}
}
