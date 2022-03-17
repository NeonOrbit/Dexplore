package io.github.neonorbit.dexplore;

public class AbortException extends RuntimeException{
  public AbortException() {
    this(null);
  }

  public AbortException(String msg) {
    super(msg, null, false, false);
  }
}
