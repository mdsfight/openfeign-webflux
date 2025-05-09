package com.zhl.cloud.openfeign.webflux.registrar;

import com.zhl.cloud.openfeign.webflux.annotation.WebfluxClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * @author zhl
 * @date 2025/5/6 10:03:00
 */
@Slf4j
public class WebClientScan extends ClassPathBeanDefinitionScanner {

    public WebClientScan(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {

        addIncludeFilter(new AnnotationTypeFilter(WebfluxClient.class));
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if(!beanDefinitionHolders.isEmpty()){
            beanDefinitionHolders.forEach(beanDefinitionHolder -> {
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
                try {
                    Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
                    WebfluxClient webfluxClient = aClass.getAnnotation(WebfluxClient.class);
                    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aClass);
                    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(webfluxClient);
                } catch (ClassNotFoundException e) {
                    log.error("class not found", e);
                }
                beanDefinition.setBeanClass(WebClientFactory.class);
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            });
        }
        return beanDefinitionHolders;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

}
