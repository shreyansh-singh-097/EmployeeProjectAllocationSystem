package com.college.config;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.Collections;
import java.util.Set;

public class FieldAttributeDialect extends AbstractProcessorDialect {

    public FieldAttributeDialect() {
        super("Field Attribute Dialect", "th", StandardDialect.PROCESSOR_PRECEDENCE);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Collections.singleton(new FieldAttributeProcessor(dialectPrefix));
    }
}
