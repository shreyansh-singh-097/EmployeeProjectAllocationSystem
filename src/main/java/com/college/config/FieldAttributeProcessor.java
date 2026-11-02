package com.college.config;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

public class FieldAttributeProcessor extends AbstractAttributeTagProcessor {

    private static final int PRECEDENCE = 900;

    public FieldAttributeProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, null, false, "field", true, PRECEDENCE, true);
    }

    @Override
    protected void doProcess(ITemplateContext context,
                             IProcessableElementTag tag,
                             AttributeName attributeName,
                             String attributeValue,
                             IElementTagStructureHandler structureHandler) {

        IStandardExpressionParser parser =
                StandardExpressions.getExpressionParser(context.getConfiguration());
        IStandardExpression expression = parser.parseExpression(context, attributeValue);
        Object value = expression.execute(context);
        String stringValue = (value != null) ? value.toString() : "";

        String fieldName = extractFieldName(attributeValue);

        structureHandler.setAttribute("id", fieldName);
        structureHandler.setAttribute("name", fieldName);
        structureHandler.setAttribute("value", stringValue);
    }

    private String extractFieldName(String expression) {
        String cleaned = expression.trim()
                .replace("*{", "").replace("${", "").replace("}", "").trim();
        int lastDot = cleaned.lastIndexOf('.');
        return (lastDot >= 0) ? cleaned.substring(lastDot + 1) : cleaned;
    }
}
