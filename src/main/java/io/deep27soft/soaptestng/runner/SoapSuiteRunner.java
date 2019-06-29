package io.deep27soft.soaptestng.runner;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestSuiteRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestSuiteRunListener;
import com.eviware.soapui.support.types.StringToObjectMap;
import io.deep27soft.soaptestng.allure.listeners.AllureSoapSuiteListener;
import io.deep27soft.soaptestng.allure.listeners.AllureSoapTestListener;
import io.deep27soft.soaptestng.utils.TestModelItemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Класс для работы с SOAP-сьютой
 */
public final class SoapSuiteRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SoapSuiteRunner.class);

    private final WsdlTestSuite testSuite;

    private final AllureSoapSuiteListener allureSuiteListener;
    private final AllureSoapTestListener allureTestListener;

    /**
     * в конструкторе soap-сьюта подготавливается к запуску:
     * удаляются все ненужные слушатели, добавляются свои из пакета allure.listeners,
     * устанавливаются параметры, если необходимо
     * @param testSuite - soap-сьюта
     * @param suiteParams - параметры, которые нужно добавить в сьюту перед запуском
     */
    public SoapSuiteRunner(WsdlTestSuite testSuite, Map<String, String> suiteParams) {
        SoapUI.setSoapUICore(new StandaloneSoapUICore(true));
        this.testSuite = testSuite;

        if (!suiteParams.isEmpty()) {
            TestModelItemUtils.setProperties(testSuite, suiteParams);
            TestModelItemUtils.logProperties(testSuite);
        }

        allureSuiteListener = new AllureSoapSuiteListener();
        removeSuiteRunListeners();
        testSuite.addTestSuiteRunListener(allureSuiteListener);
        allureTestListener = new AllureSoapTestListener();
    }

    /**
     * метод, запускающий тесты из SOAP-сьюта
     * @return - есть хотя бы один упавший тест
     */
    public boolean run() {
        LOG.info("Preparing to run suite \"{}\"", testSuite.getName());
        testSuite.getTestCaseList().forEach(testCase -> {
            WsdlTestCase wsdlTestCase = testSuite.getTestCaseByName(testCase.getName());
            removeTestRunListeners(wsdlTestCase);
            testCase.addTestRunListener(allureTestListener);
        });
        return runSuite();
    }

    /**
     * Запуск SOAP-UI-сьюты
     * @return - есть хотя бы один упавший тест
     */
    private boolean runSuite() {
        LOG.info("Running suite \"{}\"", testSuite.getName());
        WsdlTestSuiteRunner suiteRunner = testSuite.run(new StringToObjectMap(testSuite.getProperties()), false);
        return suiteRunner.getResults().stream().anyMatch(result -> result.getStatus().equals(TestRunner.Status.FAILED));
    }

    /**
     * удаляет все слушатели для сьюты
     */
    private void removeSuiteRunListeners() {
        for (TestSuiteRunListener suiteRunListener : testSuite.getTestSuiteRunListeners()) {
            testSuite.removeTestSuiteRunListener(suiteRunListener);
        }
    }

    /**
     * удаляет все слушатели для теста
     */
    private void removeTestRunListeners(WsdlTestCase wsdlTestCase) {
        for (TestRunListener testRunListener : wsdlTestCase.getTestRunListeners()) {
            wsdlTestCase.removeTestRunListener(testRunListener);
        }
    }
}
