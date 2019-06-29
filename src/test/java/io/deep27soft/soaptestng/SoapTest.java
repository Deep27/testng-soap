package io.deep27soft.soaptestng;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.SoapUIException;
import io.deep27soft.soaptestng.runner.SoapSuiteRunner;
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

        // нужно задать это свойство чтобы работали логи log4j
        System.setProperty("soapui.log4j.config",
                Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "soap-log4j.xml").toString());

        // путь до soap-проекта
        String projectPath = Paths.get(System.getProperty("user.dir"), System.getProperty("soap.project.path")).toString();

        LOG.info("Loading SoapUI project: \"{}\"", projectPath);
        // класс библиотеки com.eviware.soapui, принимающий соап-проект
        // и содержащий все необходимые поля и методы для работы с ним
        // (запуск проекта, сьюты, теста, установка параметров и т.д.)
        project = new WsdlProject(projectPath);
        LOG.info("Loaded SoapUI project: \"{}\"", project.getName());
    }

    /**
     *  метод-провайдер - достает из soap-проекта все сьюты и отправляет
     *  их по одной в testSoapSuite
     * @return - список соап-сьютов
     */
    @DataProvider
    public Iterator<Object[]> suiteProvider() {
        List<Object[]> suites = new ArrayList<>();
        // достаем сьюты из соап-проекта и складываем в List<Object[]>,
        // чтобы можно было передать на вход тестовому медоду testSoapSuite(WsdlTestSuite suite)
        project.getTestSuites().forEach((key, value) -> suites.add(new WsdlTestSuite[]{project.getTestSuiteByName(value.getName())}));
        return suites.iterator();
    }

    /**
     * метод, обрабатываемый TestNG
     * для TestNG - всегда будет пройденным, так как не содержит в себе
     * никаких проверок, а используется только для запуска соап-сьютов
     * с использованием com.eviware.soapui
     *
     * будет запущен и добавлен в allure-отчет столько раз,
     * сколько сьютов содержится в soap-проекте
     *
     * @param suite - soap-сьюта (передается методом, помеченным аннотацией @DataProvider)
     */
    @Epic("Don't pay attention to these (to be removed)")
    @Story("Служебный тест для запуска SOAP-UI сценариев")
    @Test(dataProvider = "suiteProvider")
    public void testSoapSuite(WsdlTestSuite suite) {
        LOG.info("Suite name: {}", suite.getName());
        suiteParams.clear();
        prepareSuiteParams();
        SoapSuiteRunner soapSuiteRunner = new SoapSuiteRunner(suite, suiteParams);
        boolean hasFailedTests = soapSuiteRunner.run();
        // логирование сообщения о том, что есть упавший тест
        // так как testng об ошибке не сообщит
        if (hasFailedTests) {
            LOG.error("You have failed SoapUI-tests!");
        }
    }

    /**
     * так можно устанавливать параметры для сьюты
     * (так же, если необходимо, для теста и шага)
     */
    private void prepareSuiteParams() {
        suiteParams.put("calculatorModel", "TI-82");
    }
}
