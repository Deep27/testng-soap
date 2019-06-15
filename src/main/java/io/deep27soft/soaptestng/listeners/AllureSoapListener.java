package io.deep27soft.soaptestng.listeners;

import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.testsuite.TestSuite;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.ResultsUtils;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AllureSoapListener {

    private static final String SUITE_LABEL_NAME = "suite";
    private static final String PARENT_SUITE_LABEL_NAME = "parentSuite";

    final AllureLifecycle lifecycle = Allure.getLifecycle();

    /**
     * метод для получения параметров из тестовой модели (сьюты, теста или шага)
     * @param testModelItem - TestSuite, TestCase или TestStep
     * @return - карта параметров
     */
    List<Parameter> getParameters(TestModelItem testModelItem) {
        return testModelItem.getProperties().entrySet().stream()
                .map(e -> new Parameter().withName(e.getKey()).withValue(e.getValue().getValue()))
                .collect(Collectors.toList());
    }

    /**
     * возвращает лейблы для какой-то сьюты
     * для того чтобы тесты группировались в Allure-отчете
     *
     * @param testSuite - soapui-сьюта
     * @return - список лейблов
     */
    List<Label> getLabels(TestSuite testSuite) {
        return Stream.of(
                new Label().withName(PARENT_SUITE_LABEL_NAME).withValue("SOAP-UI-Тесты"),
                new Label().withName(SUITE_LABEL_NAME).withValue(testSuite.getName()),
                new Label().withName("story").withValue("a story"),
                new Label().withName("epic").withValue("an epic"),
                new Label().withName("feature").withName("a feature"))
                .collect(Collectors.toList());
    }
}
