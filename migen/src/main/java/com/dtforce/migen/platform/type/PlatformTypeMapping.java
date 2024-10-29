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

package com.dtforce.migen.platform.type;

import com.dtforce.migen.ddl.RawTypedColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlatformTypeMapping {

	private final Map<String, Processor> processors;

	private final List<GlobalProcessor> generalProcessors;

	public PlatformTypeMapping()
	{
		processors = new HashMap<>();
		generalProcessors = new ArrayList<>();
		generalProcessors.add((dbType, chain, rawTypedColumn) -> {
			rawTypedColumn.setRawCompleteType(dbType);
			return chain.map(dbType, rawTypedColumn);
		});
	}

	public RawTypedColumn map(String dbType, RawTypedColumn column) {
		ProcessorChain processorChain = new ProcessorChain(processors, generalProcessors.listIterator());
		return processorChain.map(dbType, column);
	}

	public PlatformTypeMapping withMapping(String type, Processor processor) {
		processors.put(type, processor);
		return this;
	}

	public PlatformTypeMapping withGeneralProcessor(GlobalProcessor processor) {
		generalProcessors.add(processor);
		return this;
	}
}

class ProcessorChain implements GlobalProcessorChain
{
	private final Map<String, Processor> processors;

	private final Iterator<GlobalProcessor> leftProcessors;

	ProcessorChain(final Map<String, Processor> processors, final Iterator<GlobalProcessor> leftProcessors)
	{
		this.processors = processors;
		this.leftProcessors = leftProcessors;
	}

	@Override
	public RawTypedColumn map(final String dbType, RawTypedColumn column)
	{
		if (!leftProcessors.hasNext()) {
			if (processors.containsKey(dbType)) {
				column = processors.get(dbType).map(column);
			}
		} else {
			column = leftProcessors.next().map(dbType, new ProcessorChain(processors, leftProcessors), column);
		}
		return column;
	}
}
