package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.util.DexLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DexBasedTest {
  private DexContainer container;
  protected DexDecoder dexDecoder;

  @BeforeAll
  void setUp() {
    DexLog.enable();
    String path = Util.getResPath("classes.dex");
    this.container = new DexContainer(path, new DexOptions());
    this.dexDecoder = new DexDecoder(DexOptions.getDefault());
  }

  @AfterAll
  void tearDown() {
    this.container = null;
  }

  public List<DexEntry> getDexEntries() {
    return container.getEntries();
  }
}
