package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DexploreTest {
  private Dexplore dexplore;

  @BeforeAll
  void setUp() {
    this.dexplore = DexFactory.load(Util.getResPath("classes.dex"));
  }

  @AfterAll
  void tearDown() {
    this.dexplore = null;
  }

  @Test
  void testSearches() {
    DexFilter dexFilter = DexFilter.builder()
            .setDefinedClasses("io.neonorbit.Sample")
            .build();
    ClassFilter classFilter = ClassFilter.builder()
            .setModifiers(Modifier.PUBLIC)
            .skipModifiers(Modifier.FINAL)
            .setPackages("io.neonorbit")
            .setSourceNames("Sample.java")
            .setClasses("io.neonorbit.Sample")
            .setSuperClass("io.neonorbit.SampleSuper")
            .setInterfaces(Collections.singletonList("io.neonorbit.SampleIFace"))
            .setNumbers(1010, 201L, 301f, 401d, 501.55)
            .containsAnnotations("io.neonorbit.SampleAnnotation")
            .containsAnnotationValues("sample class", "sample method")
            .setReferenceTypes(ReferenceTypes.ALL_TYPES)
            .setReferenceFilter(pool -> pool.contains("Dex Samples") &&
                    pool.stringsContain("A unique string") &&
                    pool.typesContain("java.io.File") &&
                    pool.fieldsContain("TITLE") &&
                    pool.methodsContain("println") &&
                    pool.fieldSignaturesContain(
                            "io.neonorbit.Sample.TITLE:java.lang.String"
                    ) &&
                    pool.methodSignaturesContain(
                            "java.io.PrintStream.println(java.lang.String):void"
                    )
            )
            .build();
    MethodFilter methodFilter = MethodFilter.builder()
            .setModifiers(Modifier.PUBLIC)
            .skipModifiers(Modifier.STATIC)
            .setMethodNames("receive")
            .setParamList(Collections.singletonList("java.lang.Object"))
            .setReturnType("void")
            .containsAnnotations("io.neonorbit.SampleAnnotation")
            .containsAnnotationValues("sample method")
            .setReferenceTypes(ReferenceTypes.ALL_TYPES)
            .setReferenceFilter(pool -> pool.contains("A method sample") &&
                    pool.stringsContain("A method sample") &&
                    pool.typesContain("java.io.File") &&
                    pool.fieldsContain("out") &&
                    pool.methodsContain("println") &&
                    pool.fieldSignaturesContain(
                            "java.lang.System.out:java.io.PrintStream"
                    ) &&
                    pool.methodSignaturesContain(
                            "java.io.PrintStream.println(java.lang.String):void"
                    )
            ).build();
    Assertions.assertEquals("io.neonorbit.Sample",
            Objects.requireNonNull(dexplore.findClass(dexFilter, classFilter)).getSignature()
    );
    Assertions.assertEquals("io.neonorbit.Sample.receive(java.lang.Object):void",
            Objects.requireNonNull(dexplore.findMethod(dexFilter, classFilter, methodFilter)).getSignature()
    );
  }
}
