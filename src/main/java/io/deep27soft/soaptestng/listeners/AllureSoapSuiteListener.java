package io.deep27soft.soaptestng.listeners;

import com.eviware.soapui.model.testsuite.*;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AllureSoapSuiteListener extends AllureSoapListener implements TestSuiteRunListener {

    private static final Logger LOG = LoggerFactory.getLogger(AllureSoapSuiteListener.class);

    @Override
    public void beforeRun(TestSuiteRunner testSuiteRunner, TestSuiteRunContext testSuiteRunContext) {
        TestSuite testSuite = testSuiteRunContext.getTestSuite();
        testSuite.getTestCaseList().forEach(testCase -> {
            if (testCase.isDisabled()) {
                LOG.info("Skipping test \"{}\":\"{}\"",
                        testSuiteRunContext.getTestSuite().getName(), testCase.getName());
                final String testCaseId = UUID.randomUUID().toString();
                TestResult testResult = new TestResult()
                        .withUuid(testCaseId)
                        .withName(testCase.getName())
                        .withFullName(testSuite.getName() + ": " + testCase.getName())
                        .withParameters(getParameters(testCase))
                        .withLabels(getLabels(testSuite))
                        .withStatus(Status.SKIPPED);
                lifecycle.scheduleTestCase(testResult);
                lifecycle.startTestCase(testCaseId);
                lifecycle.stopTestCase(testCaseId);
                lifecycle.writeTestCase(testCaseId);
            }
        });
    }

    @Override
    public void afterRun(TestSuiteRunner testSuiteRunner, TestSuiteRunContext testSuiteRunContext) {

    }

    @Override
    public void beforeTestCase(TestSuiteRunner testSuiteRunner, TestSuiteRunContext testSuiteRunContext, TestCase testCase) {

    }

    @Override
    public void afterTestCase(TestSuiteRunner testSuiteRunner, TestSuiteRunContext testSuiteRunContext, TestCaseRunner testCaseRunner) {

    }
}
