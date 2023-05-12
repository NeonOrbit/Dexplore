package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.DexBasedTest;
import io.github.neonorbit.dexplore.exception.AbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.stream.StreamSupport;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MethodFilterTest extends DexBasedTest {

  @Test
  void testUniqueness() {
    Assertions.assertEquals(1, match(MethodFilter.ofMethod("getTitle")));
    Assertions.assertThrows(AbortException.class, () ->
            match(MethodFilter.ofMethod("getTitle").toBuilder().containsAnnotations("{DUMMY}").build())
    );
    Assertions.assertEquals(1, match(MethodFilter.ofMethod("getTitle").toBuilder().setParamSize(0).build()));
    Assertions.assertThrows(AbortException.class, () ->
            match(MethodFilter.ofMethod("getTitle").toBuilder()
                    .setParamSize(0).containsAnnotations("{DUMMY}").build()
            )
    );
    MethodFilter filter = MethodFilter.ofMethod(
            "receive", Collections.singletonList("java.lang.Object")
    );
    Assertions.assertEquals(1, match(filter));
    Assertions.assertThrows(AbortException.class,
            () -> match(filter.toBuilder().containsAnnotations("{DUMMY}").build())
    );
    MethodFilter filter2 = filter.toBuilder().setMethodNames("receive", "getTitle").build();
    Assertions.assertEquals(1, match(filter2));
    Assertions.assertEquals(0, match(filter2.toBuilder().containsAnnotations("{DUMMY}").build()));
    Assertions.assertEquals(2, match(filter2.toBuilder().setParamList(null).build()));
  }

  @Test
  void testConditions() {
    Assertions.assertEquals(1,
            match(MethodFilter.builder().setMethodNames("receive").setReturnType("void").build())
    );
    Assertions.assertEquals(1,
            match(MethodFilter.builder().containsAnnotations("io.neonorbit.SampleAnnotation").build())
    );
    Assertions.assertEquals(1,
            match(MethodFilter.builder().containsAnnotationValues("sample method").build())
    );
    Assertions.assertEquals(1,
            match(MethodFilter.builder().setNumbers(1010, 201L, 301f, 401d, 501.55).build())
    );
    Assertions.assertEquals(0, match(MethodFilter.builder()
                    .setMethodNames("getTitle")
                    .setModifiers(Modifier.PRIVATE).build()
            )
    );
    Assertions.assertEquals(1,
            match(MethodFilter.builder()
                    .setMethodNames("getTitle")
                    .skipModifiers(Modifier.PRIVATE).build()
            )
    );
  }

  @Test
  void testIsDefault() {
    MethodFilter filter = MethodFilter.builder()
            .setModifiers(0).skipModifiers(0).setNumbers(0)
            .setMethodNames("...").setParamSize(0).setReturnType("...")
            .setParamList(Collections.singletonList("..."))
            .containsAnnotations("...").containsAnnotationValues("...")
            .build().toBuilder()
            .setModifiers(-1).skipModifiers(-1).setNumbers()
            .setMethodNames().setParamSize(-1).setReturnType(null)
            .setParamList(null)
            .containsAnnotations().containsAnnotationValues()
            .build();
    Assertions.assertSame(filter, MethodFilter.MATCH_ALL);
  }

  private long match(MethodFilter filter) {
    return getDexEntries().stream()
            .flatMap(entry -> entry.getDexFile().getClasses().stream())
            .flatMap(dexClass -> StreamSupport.stream(dexClass.getMethods().spliterator(), false))
            .filter(dexMethod -> filter.verify(dexMethod, dexDecoder::decode))
            .count();
  }
}
