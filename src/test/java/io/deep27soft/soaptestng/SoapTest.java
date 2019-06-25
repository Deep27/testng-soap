package io.deep27soft.soaptestng;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToObjectMap;
import io.deep27soft.soaptestng.runner.SoapSuiteRunner;
import io.deep27soft.soaptestng.utils.TestModelItemUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


public class SoapTest {

    private final static Logger LOG = LoggerFactory.getLogger(SoapTest.class);

    private WsdlProject project;
    private Map<String, String> suiteParams = new HashMap<>();

    @BeforeClass
    private void setUp() throws XmlException, IOException, SoapUIException {
        System.setProperty("soapui.log4j.config",
                Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "soap-log4j.xml").toString());
        String projectPath = Paths.get(System.getProperty("user.dir"), System.getProperty("soap.project.path")).toString();
        LOG.info("Loading SoapUI project: \"{}\"", projectPath);
        project = new WsdlProject(projectPath);
        LOG.info("Loaded SoapUI project: \"{}\"", project.getName());
    }

    @DataProvider
    public Iterator<Object[]> suiteProvider() {
        List<Object[]> suites = new ArrayList<>();
        project.getTestSuites().forEach((key, value) -> suites.add(new WsdlTestSuite[]{project.getTestSuiteByName(value.getName())}));
        return suites.iterator();
    }

    @Epic("Don't pay attention to these (to be removed)")
    @Story("Служебный тест для запуска SOAP-UI сценариев")
    @Test(dataProvider = "suiteProvider")
    public void testSoapSuite(WsdlTestSuite suite) {
        LOG.info("Suite name: {}", suite.getName());
        suiteParams.clear();
        prepareSuiteParams();
        SoapSuiteRunner soapSuiteRunner = new SoapSuiteRunner(suite, suiteParams);
        boolean hasFailedTests = soapSuiteRunner.run();
    }

    private void prepareSuiteParams() {
        suiteParams.put("calculatorModel", "TI-82");
    }
}
