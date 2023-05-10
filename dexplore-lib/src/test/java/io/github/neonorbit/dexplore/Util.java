package io.github.neonorbit.dexplore;

import java.io.File;
import java.net.URL;

public class Util {
  public static String getResPath(String path) {
    URL resource = Util.class.getClassLoader().getResource(path);
    if (resource == null) {
      throw new RuntimeException("Resource not found: " + path);
    }
    return new File(resource.getPath()).getAbsolutePath();
  }
}
