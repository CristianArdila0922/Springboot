package com.logitrack.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Permite que componentes que no son gestionados por Spring (como los
 * listeners de JPA, instanciados por Hibernate) accedan al contexto de la
 * aplicacion y publiquen eventos.
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static ApplicationEventPublisher getPublisher() {
        return context;
    }

    public static <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }
}
