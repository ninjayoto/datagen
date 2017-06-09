package io.qala.datagen.junit5;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.stream.Stream;

import static io.qala.datagen.RandomShortApi.alphanumeric;

class AlphanumericArgumentProvider implements ArgumentsProvider, AnnotationConsumer<Alphanumeric> {
    private Alphanumeric annotation;

    @Override
    public void accept(Alphanumeric annotation) {
        this.annotation = annotation;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ContainerExtensionContext containerExtensionContext) throws Exception {
        int paramCount = containerExtensionContext.getTestMethod().get().getParameterCount();
        if (paramCount == 2) return Stream.of(new Object[][]{generateParams(annotation)}).map(Arguments::of);
        else                 return Stream.of(generateParam(annotation)).map(Arguments::of);
    }

    static String generateParam(Alphanumeric annotation) {
        if (annotation.length() > 0) return alphanumeric(annotation.length());
        else return alphanumeric(annotation.min(), annotation.max());
    }
    static Object[] generateParams(Alphanumeric annotation) {
        return new Object[]{generateParam(annotation), annotation.name()};
    }
}
