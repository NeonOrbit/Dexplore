package io.github.neonorbit.dexplore.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class Utils {
  public static class Lists {
    @Nullable
    public static <T> T findFirst(@Nonnull List<T> list) {
      return list.isEmpty() ? null : list.get(0);
    }
  }

  public static boolean isValidName(List<String> names) {
    return names.stream().allMatch(Utils::isValidName);
  }

  public static boolean isValidName(String name) {
    if (name == null) return false;
    for (String s : name.split("\\.", -1)) {
      if (s.isEmpty())
        return false;
      if (!Character.isJavaIdentifierStart(s.charAt(0)))
        return false;
      for (int i = 1; i < s.length(); i++) {
        if (!Character.isJavaIdentifierPart(s.charAt(i)))
          return false;
      }
    }
    return true;
  }
}
