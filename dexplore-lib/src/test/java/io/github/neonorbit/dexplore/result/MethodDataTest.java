package io.github.neonorbit.dexplore.result;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MethodDataTest {
  private List<MethodData> cases;

  @BeforeAll
  void setUp() throws NoSuchMethodException {
    cases = ImmutableList.of(
            toMethodData(String.class.getDeclaredMethod("isBlank")),
            toMethodData(String.class.getDeclaredMethod("indexOf", String.class, int.class))
    );
  }

  @Test
  void testSerialization() {
    cases.forEach(data -> Assertions.assertEquals(
            MethodData.deserialize(data.serialize()), data
    ));
  }

  private MethodData toMethodData(Method method) {
    return new MethodData(
            method.getDeclaringClass().getName(),
            method.getName(),
            Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new),
            method.getReturnType().getName()
    );
  }
}
