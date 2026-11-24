package pl.kaitoudev.tabkeeper.service.notification.email.renderer;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericTemplateRenderer {
  private final TemplateEngine templateEngine;

  public String render(String templateName, Map<String, Object> variables) {
    log.info(
        "Rendering template {} with values {} (hash={})...",
        templateName, variables, variables.hashCode()
    );

    Context context = new Context();
    context.setVariables(variables);

    String result = templateEngine.process(templateName, context);

    log.info(
        "Rendered template {} for variables of hash={}. Took {} bytes.",
        templateName, variables.hashCode(), result.getBytes().length
    );

    return result;
  }
}
