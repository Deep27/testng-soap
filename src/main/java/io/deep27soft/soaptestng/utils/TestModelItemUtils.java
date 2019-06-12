package io.deep27soft.soaptestng.utils;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.TestModelItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class TestModelItemUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestModelItemUtils.class);

    private TestModelItemUtils() {
        throw new IllegalStateException(String.format("Don't try to instantiate util class %s!", TestModelItemUtils.class.getName()));
    }

    public static void logProperties(TestModelItem testModelItem) {
        String of;
        if (testModelItem instanceof WsdlTestSuite) {
            of = "suite";
        } else if (testModelItem instanceof WsdlTestCase) {
            of = "test case";
        } else if (testModelItem instanceof WsdlTestStep) {
            of = "step";
        } else {
            throw new IllegalStateException("Don't know properties of what I'm trying to log.");
        }
        if (!testModelItem.getProperties().isEmpty()) {
            LOG.info("Properties of {} \"{}\"", of, testModelItem.getName());
            testModelItem.getProperties().forEach((key, value) -> LOG.info("\t{}: {}", key, value.getValue()));
        }
    }

    public static void setProperties(TestModelItem testModelItem, Map<String, String> properties) {
        properties.forEach(testModelItem::setPropertyValue);
    }
}