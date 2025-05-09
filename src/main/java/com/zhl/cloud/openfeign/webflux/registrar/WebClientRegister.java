package com.zhl.cloud.openfeign.webflux.registrar;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author zhl
 * @date 2025/5/6 10:01:51
 */
public class WebClientRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //自定义的 包扫描器
        WebClientScan scanHandle = new WebClientScan(beanDefinitionRegistry,false);
        //扫描指定路径下的接口
        scanHandle.doScan(ClassUtils.getPackageName(annotationMetadata.getClassName()));
    }

}
