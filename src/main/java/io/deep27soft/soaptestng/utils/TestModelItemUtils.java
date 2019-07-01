package io.deep27soft.soaptestng.utils;

import com.eviware.soapui.model.TestModelItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class TestModelItemUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestModelItemUtils.class);

    private TestModelItemUtils() {
        throw new IllegalStateException(String.format("Don't try to instantiate util class %s!", TestModelItemUtils.class.getName()));
    }

    /**
     * вывести в логах свойства объекта soap-проекта
     * @param testModelItem - сьюта, тест или шаг
     */
    public static void logProperties(TestModelItem testModelItem) {
        String of = testModelItem.getClass().getSimpleName();
        if (!testModelItem.getProperties().isEmpty()) {
            LOG.info("Properties of {} \"{}\"", of, testModelItem.getName());
            testModelItem.getProperties().forEach((key, value) -> LOG.info("\t\"{}\": \"{}\"", key, value.getValue()));
        } else {
            LOG.info("Properties for {} are absent!", of);
        }
    }

    /**
     * установить свойства для объекта соап-проекта
     * @param testModelItem - сьюта, тест или шаг
     * @param properties - свойства
     */
    public static void setProperties(TestModelItem testModelItem, Map<String, String> properties) {
        properties.forEach(testModelItem::setPropertyValue);
    }
}
