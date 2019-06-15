package io.deep27soft.soaptestng.listeners;

import com.eviware.soapui.model.testsuite.*;
import io.qameta.allure.model.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AllureSoapTestListener extends AllureSoapListener implements TestRunListener {

    private static final Logger LOG = LoggerFactory.getLogger(AllureSoapSuiteListener.class);

    private TestResult testResult;
    private String testContainerId;
    private String testCaseId;
    private String stepId;
    private StatusDetails statusDetails;
    private List<String> failMessages;
    private List<String> failTraces;

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
        logMessageOnResult(testCaseRunner);
    }

    @Override
    public void beforeStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext) {

    }

    @Override
    public void beforeStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext, TestStep testStep) {

    }

    @Override
    public void afterStep(TestCaseRunner testCaseRunner, TestCaseRunContext testCaseRunContext, TestStepResult testStepResult) {

    }

    private void prepareTestCase() {
        testCaseId = UUID.randomUUID().toString();
        testResult = new TestResult().withUuid(testCaseId);
        testContainerId = UUID.randomUUID().toString();
        statusDetails = new StatusDetails();
        failMessages = new ArrayList<>();
        failTraces = new ArrayList<>();
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
    private void logMessageOnResult(TestCaseRunner testCaseRunner) {
        final Status testCaseStatus = mapTestRunnerToAllureTestStatus(testCaseRunner.getStatus());
        final String message = "Test case \"" +
                testCaseRunner.getTestCase().getName() + "\" has finished with status: " + testCaseStatus;
        if (testCaseStatus.equals(Status.FAILED) || testCaseStatus.equals(Status.SKIPPED)) {
            LOG.error(message);
        } else {
            LOG.info(message);
        }
    }
}
