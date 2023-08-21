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
