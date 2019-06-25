package io.deep27soft.soaptestng.allure.attachment;

import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.support.types.StringToStringsMap;

import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.attachment.AttachmentContent;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.qameta.allure.attachment.http.HttpResponseAttachment;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс с методами для прикрепления к шагам allure-отчета информации о запросе и ответе
 */
public abstract class SoapAllureAttachment {

    private static FreemarkerAttachmentRenderer REQUEST_RENDERER = new FreemarkerAttachmentRenderer("http-request.ftl");
    private static FreemarkerAttachmentRenderer RESPONSE_RENDERER = new FreemarkerAttachmentRenderer("http-response.ftl");

    /**
     *  метод прицепляет информацию о запросе к шагу теста
     * @param lifecycle - AllureLifecycle, слушающий тест
     * @param stepResult - результат тестового шага
     */
    public static void attachRequest(AllureLifecycle lifecycle, WsdlTestRequestStepResult stepResult) {
        HttpRequestAttachment requestAttachment = HttpRequestAttachment.Builder
                .create("Request", stepResult.getProperty("Endpoint"))
                .withHeaders(getHeaders(stepResult.getResponseHeaders()))
                .withBody(stepResult.getRequestContentAsXml())
                .build();
        attach(requestAttachment, REQUEST_RENDERER, lifecycle);
    }

    /**
     *  метод прицепляет информацию об ответе к шагу теста
     * @param lifecycle - AllureLifecycle, слушающий тест
     * @param stepResult - результат тестового шага
     */
    public static void attachResponse(AllureLifecycle lifecycle, WsdlTestRequestStepResult stepResult) {
        WsdlResponse response = stepResult.getResponse();

        HttpResponseAttachment responseAttachment = HttpResponseAttachment.Builder
                .create("Response")
                .withUrl(response.getURL().toString())
                .withResponseCode(response.getStatusCode())
                .withHeaders(getHeaders(response.getResponseHeaders()))
                .withBody(response.getContentAsXml())
                .build();
        attach(responseAttachment, RESPONSE_RENDERER, lifecycle);
    }

    /**
     * метод прицепляет вложение к тестовому шагу
     * @param attachmentData - данные, которые нужно прикрепить
     * @param renderer - форматировщик вложения (запрос или ответ)
     * @param lifecycle - AllureLifecycle, слушающий тестовый шаг
     */
    private static void attach(AttachmentData attachmentData, FreemarkerAttachmentRenderer renderer, AllureLifecycle lifecycle) {
        AttachmentContent content = renderer.render(attachmentData);
        lifecycle.addAttachment(attachmentData.getName(), content.getContentType(), content.getFileExtension(), content.getContent().getBytes());
    }

    /**
     * форматирование заголовков из вида StringToStringsMap в HashMap
     * @param headers - StringToStringsMap
     * @return - HashMap<String, String>
     */
    private static Map<String, String> getHeaders(StringToStringsMap headers) {
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (value != null) {
                    headersMap.put(key, value.get(0));
                }
            });
        }
        return headersMap;
    }
}

