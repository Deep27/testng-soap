package io.deep27soft.soaptestng;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToObjectMap;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

//https://api.nasa.gov/planetary/apod?api_key=weYDCDbTAZcpeGhjaQco7Js37PAVcdAAqW0EediQ

public class SoapTest {

    private final static Logger LOG = LoggerFactory.getLogger(SoapTest.class);

    private WsdlProject project;

    @BeforeClass
    private void setUp() throws XmlException, IOException, SoapUIException {
        System.setProperty("soapui.log4j.config",
                Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "soap-log4j.xml").toString());
        String projectPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "soap", "Calculator-soapui-project.xml").toString();
        project = new WsdlProject(projectPath);
    }

    @DataProvider
    public Iterator<Object[]> suiteProvider() {
        List<Object[]> suites = new ArrayList<>();
        project.getTestSuites().forEach((key, value) -> suites.add(new TestSuite[]{value}));
        return suites.iterator();
    }

    @Test(dataProvider = "suiteProvider")
    public void testSoapSuite(TestSuite suite) {
        LOG.info("Suite name: {}", suite.getName());
        assertEquals(1, 1);
    }

    private void whatQMQM() throws XmlException, IOException, SoapUIException {
        WsdlProject project = new WsdlProject("");
        WsdlTestSuite suite = project.getTestSuiteAt(0);
        WsdlTestSuiteRunner runner = suite.run(new StringToObjectMap(suite.getProperties()), false);

        AllureLifecycle lc = Allure.getLifecycle();
    }
}
