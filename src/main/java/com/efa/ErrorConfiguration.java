package com.efa;

import lombok.AllArgsConstructor;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@EnableConfigurationProperties(ServerProperties.class)
@AllArgsConstructor
public class ErrorConfiguration {
    
    private final ServerProperties serverProperties;
    
    @Bean
    public ErrorController errorController(ProblemDetailsBuilder problemDetailsBuilder) {
        return new ProblemErrorController(problemDetailsBuilder, serverProperties.getError());
    }
    
    @Bean
    public SimpleProblemErrorAttributes simpleProblemErrorAttributes(ErrorAttributesConstructorFactory errorAttributesConstructorFactory) {
        return new SimpleProblemErrorAttributes(errorAttributesConstructorFactory);
    }
    
    @Bean
    public ErrorAttributesConstructorFactory errorAttributesConstructorFactory() {
        return new ErrorAttributesConstructorFactory();
    }
    
    @Bean
    public ErrorPageCustomizer errorPageCustomizer(DispatcherServletPath dispatcherServletPath) {
        return new ErrorPageCustomizer(serverProperties, dispatcherServletPath);
    }
    
    @Bean
    public static PreserveErrorControllerTargetClassPostProcessor preserveErrorControllerTargetClassPostProcessor() {
        return new PreserveErrorControllerTargetClassPostProcessor();
    }
    
    /**
     * {@link WebServerFactoryCustomizer} that configures the server's error pages.
     */
    static class ErrorPageCustomizer implements ErrorPageRegistrar, Ordered {
        
        private final ServerProperties properties;
        
        private final DispatcherServletPath dispatcherServletPath;
        
        protected ErrorPageCustomizer(ServerProperties properties, DispatcherServletPath dispatcherServletPath) {
            this.properties = properties;
            this.dispatcherServletPath = dispatcherServletPath;
        }
        
        @Override
        public void registerErrorPages(ErrorPageRegistry errorPageRegistry) {
            final ErrorPage errorPage = new ErrorPage(dispatcherServletPath.getRelativePath(properties.getError().getPath()));
            errorPageRegistry.addErrorPages(errorPage);
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
        
    }
    
    /**
     * {@link BeanFactoryPostProcessor} to ensure that the target class of ErrorController
     * MVC beans are preserved when using AOP.
     */
    static class PreserveErrorControllerTargetClassPostProcessor implements BeanFactoryPostProcessor {
        
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            final String[] errorControllerBeans = beanFactory.getBeanNamesForType(ErrorController.class, false, false);
            for (final String errorControllerBean : errorControllerBeans) {
                try {
                    beanFactory.getBeanDefinition(errorControllerBean).setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
                } catch (Throwable ex) {
                    // Ignore
                }
            }
        }
        
    }
    
}
