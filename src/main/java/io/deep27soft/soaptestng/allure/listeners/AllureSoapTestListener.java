package io.deep27soft.soaptestng.allure.listeners;

import com.eviware.soapui.impl.wsdl.teststeps.JdbcTestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.RestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.model.testsuite.*;
import io.qameta.allure.model.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.deep27soft.soaptestng.allure.attachment.SoapAllureAttachment.attachRequest;
import static io.deep27soft.soaptestng.allure.attachment.SoapAllureAttachment.attachResponse;

public final class AllureSoapTestListener extends AllureSoapListener implements TestRunListener {

    private static final Logger LOG = LoggerFactory.getLogger(AllureSoapSuiteListener.class);

    private TestResult testResult;
    private String testContainerId;
    private String testCaseId;
    private String stepId;
    private StatusDetails statusDetails;
    private List<String> failMessages;
    private List<String> failTraces;

    /**
     *  метод вызывается перед запуском теста
     *  подготавливает тест и стартует его, используя allureLifecycle
     */
    @Override
    public void beforeRun(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        TestCase testCase = testCaseRunContext.getTestCase();
        TestSuite testSuite = testCase.getTestSuite();
        prepareTestCase();
        testResult = testResult.withName(testCase.getName())
                .withFullName(testSuite.getName() + ":" + testCase.getName())
                .withParameters(getParameters(testCase))
                .withLabels(getLabels(testSuite));
        lifecycle.scheduleTestCase(testResult);

        FixtureResult afterResult = new FixtureResult()
                .withStatus(Status.PASSED);
        TestResultContainer testResultContainer = new TestResultContainer()
                .withUuid(testContainerId)
                .withAfters(afterResult)
                .withName("Test result container")
                .withChildren(testCaseId);
        lifecycle.startTestContainer(testResultContainer);
        lifecycle.startTestCase(testCaseId);
    }

    /**
     *  метод вызывается после запуска теста
     *  обрабатываются результаты теста (сообщения об ошибках, если он упал), обновляется статус
     *  и allureLifecycle его завершает (тест добавлится в отчет)
     */
    @Override
    public void afterRun(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        if(!failMessages.isEmpty()) {
            statusDetails.setMessage(StringUtils.join(failMessages, "\n"));
        }
        if (!failTraces.isEmpty()) {
            statusDetails.setTrace(StringUtils.join(failTraces, "\n"));
        }
        final Status testCaseStatus = mapTestRunnerToAllureTestStatus(testCaseRunner.getStatus());
        lifecycle.updateTestCase(testCaseId, testCase -> testCase.withStatus(testCaseStatus));
        lifecycle.stopTestCase(testCaseId);
        lifecycle.writeTestCase(testCaseId);
        lifecycle.stopTestContainer(testContainerId);
        lifecycle.writeTestContainer(testContainerId);
        logTestMessageOnResult(testCaseRunner);
    }

    @Override
    public void beforeStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {
        // not used
    }

    /**
     *  метод срабатывает перед запуском каждого тестового шага
     *  allureLifecycle подготавливает тестовый шаг для добавления его в отчет
     */
    @Override
    public void beforeStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext, TestStep testStep) {
        stepId = UUID.randomUUID().toString();
        lifecycle.startStep(testCaseId, stepId,
                new StepResult().withName(testStep.getName()).withStatus(Status.PASSED));
    }

    /**
     * метод срабатывает после завершения каждого тестового шага
     * обрабатываются результаты тестового шага и тест завершается allureLifecycle'ом
     * (готов быть добавленным в отчет)
     */
    @Override
    public void afterStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext, TestStepResult testStepResult) {
        Status status = mapStepToAllureStepStatus(testStepResult.getStatus());
        processStepResults(testStepResult, testCaseRunner);
        lifecycle.updateStep(stepId, step ->
                step.withStatus(status)
                        .withDescription("step description")
                        .withDescriptionHtml("step html description")
                        .withStage(Stage.FINISHED)
                        .withStatusDetails(statusDetails));
        testResult.setStatusDetails(statusDetails);
        lifecycle.stopStep(stepId);
    }

    /**
     * обработка результатов тестового шага (прицепление вложений и сбор ошибок)
     * @param stepResult - результаты шага
     * @param testCaseRunner - test case runner
     */
    private void processStepResults(TestStepResult stepResult, TestCaseRunner testCaseRunner) {
        Status stepStatus = mapStepToAllureStepStatus(stepResult.getStatus());
        StringBuilder message = new StringBuilder(String.format(
                "Step \"%s\" has finished with status: %s", stepResult.getTestStep().getName(), stepStatus.toString()));
        message.append(String.format("\n\tMessages:\n\t\t%s",
                ArrayUtils.isEmpty(stepResult.getMessages()) ? "none" : StringUtils.join(stepResult.getMessages(), '\n')));
        String endpoint = "-";
        String request = "-";
        String response = "-";
        if (stepResult instanceof WsdlTestRequestStepResult) {
            attachRequest(lifecycle, (WsdlTestRequestStepResult) stepResult);
            attachResponse(lifecycle, (WsdlTestRequestStepResult) stepResult);
            endpoint = ((WsdlTestRequestStepResult) stepResult).getProperties().get("URL");
            request = ((WsdlTestRequestStepResult) stepResult).getRequestContentAsXml();
            response = ((WsdlTestRequestStepResult) stepResult).getResponseContentAsXml();
        } else if (stepResult instanceof RestRequestStepResult) {
            // если в шаге вместо SOAP был REST-запрос
//            RequestResponseAttachment attachments = new RequestResponseAttachment((RestRequestStepResult) testStepResult);
//            attachments.attachRequest(lifecycle);
//            attachments.attachResponse(lifecycle);
//            endpoint = ((RestRequestStepResult) testStepResult).getProperties().get("URL");
//            request = ((RestRequestStepResult) testStepResult).getRequestContent();
//            response = ((RestRequestStepResult) testStepResult).getResponseContent();
        } else if (stepResult instanceof JdbcTestStepResult) {
            // если был запрос в бд
//            request = ((JdbcTestStepResult) testStepResult).getRequestContent();
//            response = ((JdbcTestStepResult) testStepResult).getResponseContentAsXml();
        } else {
            lifecycle.updateStep(stepId, step -> step
                    .withParameters(getParameters(stepResult.getTestStep())));
        }
        message.append(String.format("\n\tEndpoint: %s\n\tRequest:\n\t\t%s\n\tResponse:\n\t\t%s", endpoint, request, response));
        if (mapStepToAllureStepStatus(stepResult.getStatus()).equals(Status.FAILED)) {
            String[] messages = stepResult.getMessages();
            String testStepName = "\"" + stepResult.getTestStep().getName() + "\" -> ";
            if (messages.length != 0) {
                failMessages.add(testStepName + Arrays.toString(stepResult.getMessages()));
            } else {
                failMessages.add(testStepName + testCaseRunner.getReason());
            }
            Optional.ofNullable(stepResult.getError()).ifPresent(e -> failTraces.add(testStepName + e.toString()));
        }
        logStepMessageOnResult(message.toString(), stepStatus);
    }

    /**
     * Подготовка тест-кейса
     */
    private void prepareTestCase() {
        testCaseId = UUID.randomUUID().toString();
        testResult = new TestResult().withUuid(testCaseId);
        testContainerId = UUID.randomUUID().toString();
        statusDetails = new StatusDetails();
        failMessages = new ArrayList<>();
        failTraces = new ArrayList<>();
    }

    /**
     * метод мапит статус библиотеки soapui на статус библиотеки allure
     * @param stepStatus - статус soapui
     * @return - статус allure
     */
    private Status mapStepToAllureStepStatus(TestStepResult.TestStepStatus stepStatus) {
        switch (stepStatus) {
            case FAILED:
                return Status.FAILED;
            case CANCELED:
                return Status.SKIPPED;
            case OK:
                return Status.PASSED;
            default:
                return Status.BROKEN;
        }
    }

    /**
     * метод мапит статус testRunner'a на статус библиотеки allure
     * @param testRunnerStatus статус testRunner
     * @return - статус allure
     */
    private Status mapTestRunnerToAllureTestStatus(TestRunner.Status testRunnerStatus) {
        switch (testRunnerStatus) {
            case FAILED:
                return Status.FAILED;
            case CANCELED:
                return Status.SKIPPED;
            case FINISHED:
                return Status.PASSED;
            default:
                return Status.BROKEN;
        }
    }

    /**
     * вывести сообщение или ошибку в зависимости от статуса
     * @param testCaseRunner - testCaseRunner
     */
    private void logTestMessageOnResult(TestCaseRunner testCaseRunner) {
        final Status testCaseStatus = mapTestRunnerToAllureTestStatus(testCaseRunner.getStatus());
        final String message = "Test case \"" +
                testCaseRunner.getTestCase().getName() + "\" has finished with status: " + testCaseStatus;
        if (testCaseStatus.equals(Status.FAILED) || testCaseStatus.equals(Status.SKIPPED)) {
            LOG.error(message);
        } else {
            LOG.info(message);
        }
    }

    /**
     * вывести сообщение или ошибку в зависимости от статуса шага
     * @param message - сообщение
     * @param status - статус
     */
    private void logStepMessageOnResult(String message, Status status) {
        if (status.equals(Status.FAILED)) {
            LOG.error(message);
        } else {
            LOG.info(message);
        }
    }
}
