package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.DexBasedTest;
import io.github.neonorbit.dexplore.DexEntry;
import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.exception.AbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DexFilterTest extends DexBasedTest {

  @Test
  void testPreferredDex() {
    DexEntry dexEntry = Mockito.mock(DexEntry.class);
    Mockito.when(dexEntry.getDexName()).thenReturn("classes5.dex").thenReturn("classes7.dex");
    DexFilter dexFilter = DexFilter.builder()
            .setPreferredDexNames("classes5.dex")
            .allowPreferredDexOnly(true)
            .build();
    Assertions.assertTrue(dexFilter.verify(dexEntry, getDecoder()));
    Assertions.assertThrows(AbortException.class, () -> dexFilter.verify(dexEntry, getDecoder()));
  }

  @Test
  void testUniqueness() {
    DexFilter filter = DexFilter.builder().setDefinedClasses("io.neonorbit.Sample").build();
    Assertions.assertTrue(verify(filter));
    DexFilter filter2 = filter.toBuilder().setStoredSources("{DUMMY}").build();
    Assertions.assertThrows(AbortException.class, () -> verify(filter2));
    DexFilter filter3 = filter.toBuilder().setDefinedClasses(
            "io.neonorbit.SamplePrimary", "io.neonorbit.SampleSecondary"
    ).build();
    Assertions.assertTrue(verify(filter3));
  }

  @Test
  void testConditions() {
    Assertions.assertTrue(verify(DexFilter.builder().setStoredSources("Sample.java").build()));
    Assertions.assertFalse(verify(DexFilter.builder().setStoredSources("{DUMMY}").build()));
    Assertions.assertTrue(verify(DexFilter.builder().setStoredSources("Sample.java").build()));
  }

  @Test
  void testIsDefault() {
    DexFilter filter = DexFilter.builder()
            .setPreferredDexNames("...").setDefinedClasses("...").setStoredSources("...")
            .allowPreferredDexOnly(true)
            .build().toBuilder()
            .setPreferredDexNames().setDefinedClasses().setStoredSources()
            .allowPreferredDexOnly(false)
            .build();
    Assertions.assertSame(filter, DexFilter.MATCH_ALL);
  }

  private boolean verify(DexFilter filter) {
    return getDexEntries().stream().anyMatch(e -> filter.verify(e, dexDecoder::decode));
  }

  private <T> LazyDecoder<T> getDecoder() {
    return (i, t) -> null;
  }
}
