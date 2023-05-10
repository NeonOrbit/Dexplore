package io.github.neonorbit.dexplore.result;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FieldDataTest {
  private List<FieldData> cases;

  @BeforeAll
  void setUp() throws NoSuchFieldException {
    cases = ImmutableList.of(
            toFieldData(String.class.getDeclaredField("value")),
            toFieldData(HashSet.class.getDeclaredField("map"))
    );
  }

  @Test
  void testSerialization() {
    cases.forEach(data -> Assertions.assertEquals(
            FieldData.deserialize(data.serialize()), data
    ));
  }

  private FieldData toFieldData(Field Field) {
    return new FieldData(
            Field.getDeclaringClass().getName(),
            Field.getName(),
            Field.getType().getName()
    );
  }
}
