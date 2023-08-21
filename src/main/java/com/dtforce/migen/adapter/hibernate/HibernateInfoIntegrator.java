package com.dtforce.migen.adapter.hibernate;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class HibernateInfoIntegrator implements Integrator
{

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    	HibernateInfoHolder hibernateInfoHolder = HibernateInfoHolder.INSTANCE;
		hibernateInfoHolder.setMetadata(metadata);
		hibernateInfoHolder.setSessionFactory(sessionFactory);
		hibernateInfoHolder.setServiceRegistry(serviceRegistry);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }
}
