package core.mvc.tobe;

import com.google.common.collect.Maps;
import core.annotation.web.RequestMethod;
import core.di.BeanScanner;
import core.di.ConfigurationBeanScanner;
import core.di.factory.BeanFactory;
import core.mvc.HandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class AnnotationHandlerMapping implements HandlerMapping {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final BeanScanner beanScanner;
    private final ConfigurationBeanScanner configurationBeanScanner;

    private final Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        BeanFactory beanFactory = new BeanFactory();
        beanScanner = new BeanScanner(beanFactory, basePackage);
        configurationBeanScanner = new ConfigurationBeanScanner(beanFactory);
    }

    public void initialize() {
        logger.info("## Initialized Annotation Handler Mapping");
        handlerExecutions.putAll(beanScanner.scan());
        configurationBeanScanner.register();
    }

    public Object getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        logger.debug("requestUri : {}, requestMethod : {}", requestUri, rm);
        return getHandlerInternal(new HandlerKey(requestUri, rm));
    }

    private HandlerExecution getHandlerInternal(HandlerKey requestHandlerKey) {
        return handlerExecutions.keySet().stream()
                .filter(handlerKey -> handlerKey.isMatch(requestHandlerKey))
                .findFirst()
                .map(handlerExecutions::get).orElse(null);

    }
}
