package io.github.neonorbit.dexplore.util;

import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class DexUtilsTest {
  @Mock
  private DexBackedMethod dexMethod;

  private static final String[] JAVA = new String[] {
          "java.lang.Object" ,
          "[Ljava.lang.Object;",
          "java.lang.Object$Inner",
          "[[Ljava.lang.Object$Inner;"
  };

  private static final String[] DEX = new String[] {
          "Ljava/lang/Object;",
          "[Ljava/lang/Object;",
          "Ljava/lang/Object$Inner;",
          "[[Ljava/lang/Object$Inner;"
  };

  @Test
  void testTypeConversation() {
    for (int i = 0; i < JAVA.length; i++) {
      Assertions.assertEquals(JAVA[i], DexUtils.dexToJavaTypeName(DEX[i]));
      Assertions.assertEquals(DEX[i], DexUtils.javaToDexTypeName(JAVA[i]));
      Assertions.assertEquals(JAVA[i], DexUtils.dexToJavaTypeName(DexUtils.javaToDexTypeName(JAVA[i])));
    }
  }

  @Test
  void testDexToJavaParamExtraction() {
    Mockito.when(dexMethod.getParameterTypes()).thenReturn(Arrays.asList(DEX));
    Assertions.assertArrayEquals(JAVA, DexUtils.getJavaParams(dexMethod));
    Mockito.verifyNoMoreInteractions(dexMethod);
  }
}
