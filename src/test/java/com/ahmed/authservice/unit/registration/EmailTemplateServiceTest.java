package com.ahmed.authservice.unit.registration;

import com.ahmed.authservice.registration.EmailTemplateService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailTemplateServiceTest {


    @Test
    void shouldBuildEmailTemplateSuccessfully() {
        TemplateEngine engine = mock(TemplateEngine.class);

        when(engine.process(eq("email-template"), any(Context.class)))
                .thenReturn("<html>Hi John</html>");

        EmailTemplateService service = new EmailTemplateService(engine);

        String result = service.buildEmail("John", "http://link");

        assertEquals("<html>Hi John</html>", result);
    }

    @Test
    void shouldUseCorrectTemplateName() {
        TemplateEngine engine = mock(TemplateEngine.class);

        EmailTemplateService service = new EmailTemplateService(engine);

        service.buildEmail("John", "http://link");

        verify(engine).process(eq("email-template"), any(Context.class));
    }

    @Test
    void shouldPassCorrectVariablesToTemplate() {
        TemplateEngine engine = mock(TemplateEngine.class);

        EmailTemplateService service = new EmailTemplateService(engine);

        service.buildEmail("John", "http://link");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);

        verify(engine).process(eq("email-template"), captor.capture());

        Context ctx = captor.getValue();

        assertEquals("John", ctx.getVariable("name"));
        assertEquals("http://link", ctx.getVariable("link"));
    }

    @Test
    void shouldHandleNullInputsGracefully() {
        TemplateEngine engine = mock(TemplateEngine.class);

        when(engine.process(anyString(), any(Context.class)))
                .thenReturn("<html>error</html>");

        EmailTemplateService service = new EmailTemplateService(engine);

        assertDoesNotThrow(() ->
                service.buildEmail(null, null)
        );
    }

    @Test
    void shouldHandleEmptyValues() {
        TemplateEngine engine = mock(TemplateEngine.class);

        when(engine.process(anyString(), any(Context.class)))
                .thenReturn("<html>empty</html>");

        EmailTemplateService service = new EmailTemplateService(engine);

        String result = service.buildEmail("", "");

        assertNotNull(result);
    }
}