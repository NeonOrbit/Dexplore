package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.filter.ReferenceFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DexDecoderTest extends DexBasedTest {

  @Test
  void testDexFileReferences() {
    ReferencePool pool = DexDecoder.decodeFully(getDexEntries().get(0).getDexFile());
    Assertions.assertTrue(pool.contains("Dex Samples"));
    Assertions.assertTrue(pool.stringsContain("A unique string"));
    Assertions.assertTrue(pool.typesContain("java.io.File"));
    Assertions.assertTrue(pool.fieldsContain("TITLE"));
    Assertions.assertTrue(pool.methodsContain("println"));
    Assertions.assertTrue(pool.fieldSignaturesContain(
            "io.neonorbit.Sample.TITLE:java.lang.String"
    ));
    Assertions.assertTrue(pool.methodSignaturesContain(
            "java.io.PrintStream.println(java.lang.String):void"
    ));
  }

  @Test
  void testDexClassReferences() {
    Assertions.assertEquals(1, match(ReferenceFilter.contains("A unique string")));
    Assertions.assertEquals(1, match(ReferenceFilter.stringsContain("A unique string")));
    Assertions.assertEquals(1, match(ReferenceFilter.typesContain("java.io.File")));
    Assertions.assertEquals(1, match(ReferenceFilter.fieldsContain("TITLE")));
    Assertions.assertEquals(1, match(ReferenceFilter.methodsContain("println")));
    Assertions.assertEquals(1, match(pool -> pool.fieldSignaturesContain(
            "io.neonorbit.Sample.TITLE:java.lang.String"
    )));
    Assertions.assertEquals(1, match(pool -> pool.methodSignaturesContain(
            "java.io.PrintStream.println(java.lang.String):void"
    )));
  }

  private long match(ReferenceFilter filter) {
    return getDexEntries().stream()
            .flatMap(entry -> entry.getDexFile().getClasses().stream())
            .filter(dexClass -> filter.accept(DexDecoder.decodeFully(dexClass)))
            .count();
  }
}
