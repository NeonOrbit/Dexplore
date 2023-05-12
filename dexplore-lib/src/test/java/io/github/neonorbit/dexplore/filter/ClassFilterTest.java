package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.DexBasedTest;
import io.github.neonorbit.dexplore.exception.AbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassFilterTest extends DexBasedTest {

  @Test
  void testUniqueness() {
    ClassFilter filter = ClassFilter.builder().setClasses("io.neonorbit.Sample").build();
    Assertions.assertEquals(1, match(filter));
    ClassFilter filter2 = filter.toBuilder().setSourceNames("{DUMMY}").build();
    Assertions.assertThrows(AbortException.class, () -> match(filter2));
    ClassFilter filter3 = filter2.toBuilder().setClasses(
            "io.neonorbit.SamplePrimary", "io.neonorbit.SampleSecondary"
    ).build();
    Assertions.assertEquals(0, match(filter3));
    Assertions.assertEquals(2, match(filter3.toBuilder().setSourceNames().build()));
  }

  @Test
  void testConditions() {
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setClasses("io.neonorbit.Sample").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setClassSimpleNames("Sample").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setSourceNames("Sample.java").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setSuperClass("io.neonorbit.SampleSuper").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setInterfaces(Collections.singletonList("io.neonorbit.SampleIFace")).build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().containsAnnotations("io.neonorbit.SampleAnnotation").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().containsAnnotationValues("sample class", "sample method").build())
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder().setNumbers(1010, 201L, 301f, 401d, 501.55).build())
    );
    Assertions.assertEquals(2,
            match(ClassFilter.builder()
                    .setPackages("io.neonorbit")
                    .setClassSimpleNames("Sample", "SampleSubPkg", "SampleExtra")
                    .build()
            )
    );
    Assertions.assertEquals(1,
            match(ClassFilter.builder()
                    .skipPackages(
                            Arrays.asList("io.neonorbit", "xyz"),
                            Collections.singletonList("io.neonorbit.sub")
                    ).setClassSimpleNames("Sample", "SampleSubPkg", "SampleExtra")
                    .build()
            )
    );
    Assertions.assertEquals(1, match(ClassFilter.builder().setModifiers(Modifier.FINAL).build()));
    Assertions.assertEquals(0, match(ClassFilter.builder().skipModifiers(Modifier.PUBLIC).build()));
  }

  @Test
  void testIsDefault() {
    ClassFilter filter = ClassFilter.builder()
            .setModifiers(0).skipModifiers(0).setNumbers(0)
            .setPackages("io").skipPackages(Collections.singletonList("io"), null)
            .setSuperClass("...").setClasses("...")
            .setInterfaces(Collections.singletonList("..."))
            .containsAnnotations("...").containsAnnotationValues("...")
            .build().toBuilder()
            .setModifiers(-1).skipModifiers(-1).setNumbers()
            .setPackages().skipPackages(null, null)
            .setSuperClass(null).setClasses().setClassSimpleNames()
            .setInterfaces(null)
            .containsAnnotations().containsAnnotationValues()
            .build();
    Assertions.assertSame(filter, ClassFilter.MATCH_ALL);
  }

  private long match(ClassFilter filter) {
    return getDexEntries().stream()
            .flatMap(entry -> entry.getDexFile().getClasses().stream())
            .filter(dexClass -> filter.verify(dexClass, dexDecoder::decode))
            .count();
  }
}
