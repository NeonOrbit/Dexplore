package io.github.neonorbit.dexplore.result;

import com.google.common.collect.ImmutableList;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ClassDataTest {
  private static final List<ClassData> CASES = ImmutableList.of(
          new ClassData(String.class.getName()),
          new ClassData(String[][].class.getName()),
          new ClassData(ClassFilter.Builder.class.getName())
  );

  @Test
  void testSerialization() {
    CASES.forEach(data -> Assertions.assertEquals(
            data, ClassData.deserialize(data.serialize())
    ));
  }
}
