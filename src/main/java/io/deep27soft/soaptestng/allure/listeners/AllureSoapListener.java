package io.deep27soft.soaptestng.allure.listeners;

import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.testsuite.TestSuite;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * родительский класс для слушателей soap-тестов
 */
abstract class AllureSoapListener {

    // AllureLIfecycle содержит в себе Allure-контекст и методы для работы с ним
    // Позволяет работать со сьютами, тестами и шагами, для добавления их в отчет и модификации
    final AllureLifecycle lifecycle = Allure.getLifecycle();

    /**
     * метод для получения параметров из тестовой TestRunListenerмодели (сьюты, теста или шага)
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
     * @return - список лейбловTestRunListener
     */
    List<Label> getLabels(TestSuite testSuite) {
        return Stream.of(
                new Label().withName("parentSuite").withValue("SOAP-UI-Тесты"),
                new Label().withName("suite").withValue(testSuite.getName()),
                new Label().withName("story").withValue(testSuite.getLabel()),
                new Label().withName("epic").withValue("SOAP UI"),
                new Label().withName("feature").withName("a feature"))
                .collect(Collectors.toList());
    }
}
