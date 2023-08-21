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

package com.dtforce.migen.adapter.hibernate;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

@Getter
@Setter
public class HibernateInfoHolder
{

	public static HibernateInfoHolder INSTANCE = new HibernateInfoHolder();

	private Metadata metadata;

	private SessionFactoryImplementor sessionFactory;

	private SessionFactoryServiceRegistry serviceRegistry;

	private HibernateInfoHolder()
	{

	}

}
