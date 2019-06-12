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
import io.deep27soft.soaptestng.listeners.AllureSoapSuiteListener;
import io.deep27soft.soaptestng.listeners.AllureSoapTestListener;
import io.deep27soft.soaptestng.utils.TestModelItemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Класс для работы с SOAP-сьютой
 * Created by sotnichenko-r on 21.02.2019.
 */
public final class SoapSuiteRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SoapSuiteRunner.class);

    private final WsdlTestSuite testSuite;

    private final AllureSoapSuiteListener allureSuiteListener;
    private final AllureSoapTestListener allureTestListener;

    public SoapSuiteRunner(WsdlTestSuite testSuite, Map<String, String> suiteParams) {
        SoapUI.setSoapUICore(new StandaloneSoapUICore(true));
        this.testSuite = testSuite;

        TestModelItemUtils.setProperties(testSuite, suiteParams);
        TestModelItemUtils.logProperties(testSuite);

        allureSuiteListener = new AllureSoapSuiteListener();
        testSuite.addTestSuiteRunListener(allureSuiteListener);
        allureTestListener = new AllureSoapTestListener();
        testSuite.getTestCaseList().forEach(testCase ->  testCase.addTestRunListener(allureTestListener));
    }

    /**
     * метод, запускающий тесты из SOAP-сьюта
     * @return - есть хотя бы один упавший тест
     */
    public boolean run() {
        removeSuiteRunListeners();
        testSuite.getTestCaseList().forEach(testCase -> {
            WsdlTestCase wsdlTestCase = testSuite.getTestCaseByName(testCase.getName());
            removeTestRunListeners(wsdlTestCase);
        });
        return runSuite();
    }

    /**
     * Запуск SOAP-UI-сьюты
     * @return - есть хотя бы один упавший тест
     */
    private boolean runSuite() {
        WsdlTestSuiteRunner suiteRunner = testSuite.run(new StringToObjectMap(testSuite.getProperties()), false);
        return suiteRunner.getResults().stream().anyMatch(result -> result.getStatus().equals(TestRunner.Status.FAILED));
    }

    /**
     *  метод устанавливает флаг disabled для всех тестов в сьюте
     *  и запускает runner, для того, чтобы AllureLifecycle смог
     *  добавить пропущенные тесты в отчет
     */
    public void ignore() {
        removeSuiteRunListeners();
        testSuite.getTestCaseList().forEach(testCase -> {
            WsdlTestCase wsdlTestCase = testSuite.getTestCaseByName(testCase.getName());
            removeTestRunListeners(wsdlTestCase);
            wsdlTestCase.setDisabled(true);
        });
    }

    /**
     * удаляет все слушатели у сьюты
     */
    private void removeSuiteRunListeners() {
        for (TestSuiteRunListener suiteRunListener : testSuite.getTestSuiteRunListeners()) {
            testSuite.removeTestSuiteRunListener(suiteRunListener);
        }
    }

    /**
     * удаляет все слушатели у теста
     */
    private void removeTestRunListeners(WsdlTestCase wsdlTestCase) {
        for (TestRunListener testRunListener : wsdlTestCase.getTestRunListeners()) {
            wsdlTestCase.removeTestRunListener(testRunListener);
        }
    }
}
