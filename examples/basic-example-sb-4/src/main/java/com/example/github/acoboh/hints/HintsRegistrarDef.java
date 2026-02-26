package com.example.github.acoboh.hints;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.accept.DefaultApiVersionStrategy;
import org.springframework.web.accept.HeaderApiVersionResolver;

import java.util.List;

// Temporary class to avoid bug with SpringDoc on GraalVM 25
@Configuration
@ImportRuntimeHints(HintsRegistrarDef.CustomRuntimeHints.class)
public class HintsRegistrarDef {

    HintsRegistrarDef() {

    }

    static class CustomRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(
                    TypeReference.of("org.springdoc.core.providers.SpringWebProvider$$SpringCGLIB$$0"),
                    builder -> builder.withField("CGLIB$FACTORY_DATA"));

            hints.reflection().registerType(
                    TypeReference.of("org.springdoc.core.providers.SpringWebProvider$$SpringCGLIB$$0"),
                    builder -> builder.withField("CGLIB$CALLBACK_FILTER"));

            hints.reflection().registerType(
                    TypeReference.of("org.springdoc.core.providers.SpringWebProvider$$SpringCGLIB$$0"),
                    builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS));

            hints.reflection().registerType(TypeReference.of(DefaultApiVersionStrategy.class),
                    builder -> builder.withField("versionResolvers"));

            hints.reflection().registerType(TypeReference.of(HeaderApiVersionResolver.class),
                    builder -> builder.withField("headerName"));

            hints.reflection().registerType(org.springdoc.core.providers.SpringWebProvider.class,
                    typeHint -> typeHint.withMethod("findPathPrefix",
                            List.of(TypeReference.of(org.springdoc.core.properties.SpringDocConfigProperties.class)),
                            ExecutableMode.INVOKE));
        }
    }

}
