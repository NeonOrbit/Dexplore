/*
 * Copyright (C) 2022 NeonOrbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.AbortException;
import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MethodFilter extends BaseFilter<DexBackedMethod> {
  private final int flag;
  private final int skipFlag;
  private final int paramSize;
  private final String returnType;
  private final List<String> paramList;
  private final Set<String> inMethodNames;

  private final boolean isUnique;
  private final String methodName;

  private MethodFilter(Builder builder) {
    super(builder);
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.paramSize = builder.paramSize;
    this.paramList = builder.paramList;
    this.returnType = builder.returnType;
    this.inMethodNames = builder.inMethodNames;
    if (builder.inMethodNames == null ||
        builder.inMethodNames.size() != 1) {
      this.methodName = null;
    } else {
      this.methodName = builder.inMethodNames.iterator().next();
    }
    this.isUnique = methodName != null && (paramSize == 0 || paramList != null);
  }

  public boolean isUnique() {
    return isUnique;
  }

  @Override
  public boolean verify(@Nonnull DexBackedMethod dexMethod,
                        @Nonnull LazyDecoder<DexBackedMethod> decoder) {
    if(!checkMethodSignature(dexMethod)) return false;
    boolean result = (
            (flag == 0 || (dexMethod.accessFlags & flag) == flag) &&
            (skipFlag == 0 || (dexMethod.accessFlags & skipFlag) == 0) &&
            (returnType == null || returnType.equals(dexMethod.getReturnType())) &&
            super.verify(dexMethod, decoder)
    );
    if (isUnique && !result) {
      throw new AbortException("Method found but the filter didn't match");
    }
    return result;
  }

  private boolean checkMethodSignature(DexBackedMethod dexMethod) {
    if (!checkMethodName(dexMethod)) {
      return false;
    } else if (paramList != null) {
      List<String> list = dexMethod.getParameterTypes();
      return paramList.size() == list.size() && list.equals(paramList);
    }
    return paramSize < 0 || dexMethod.getParameterTypes().size() == paramSize;
  }

  private boolean checkMethodName(DexBackedMethod dexMethod) {
    if (methodName != null)
      return methodName.equals(dexMethod.getName());
    if (inMethodNames != null)
      return inMethodNames.contains(dexMethod.getName());
    return true;
  }

  public static MethodFilter ofMethod(@Nonnull String method,
                                      @Nonnull String... paramList) {
    return builder().setMethodNames(Objects.requireNonNull(method))
                    .setParamList(Objects.requireNonNull(paramList))
                    .build();
  }

  public static MethodFilter none() {
    return builder().build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, MethodFilter> {
    private int flag;
    private int skipFlag;
    private int paramSize = -1;
    private String returnType;
    private List<String> paramList;
    private Set<String> inMethodNames;

    public Builder() {}

    private Builder(MethodFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.paramSize = instance.paramSize;
      this.paramList = instance.paramList;
      this.returnType = instance.returnType;
      this.inMethodNames = instance.inMethodNames;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public MethodFilter build() {
      return new MethodFilter(this);
    }

    public Builder setMethodNames(@Nullable String... methods) {
      this.inMethodNames = methods == null || methods.length == 0 ? null :
                           new HashSet<>(Arrays.asList(methods));
      return this;
    }

    /**
     * Set method modifiers. eg: public, static, final etc...
     * @param modifiers see {@link java.lang.reflect.Modifier}
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Methods with matching modifiers will be skipped
     * @param modifiers see {@link Builder#setModifiers(int)}
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    public Builder setReturnType(@Nullable String returnType) {
      this.returnType = returnType == null ? null :
                        DexUtils.javaToDexTypeName(returnType);
      return this;
    }

    public Builder setParamSize(int paramSize) {
      this.paramSize = paramSize;
      return this;
    }

    public Builder setParamList(@Nullable String... paramList) {
      if (paramList == null)
        this.paramList = null;
      else if (paramList.length == 0)
        this.paramSize = 0;
      else
        this.paramList = DexUtils.javaToDexTypeName(Arrays.asList(paramList));
      return this;
    }
  }
}
