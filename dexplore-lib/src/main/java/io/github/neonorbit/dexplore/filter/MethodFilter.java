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

import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.util.AbortException;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Internal;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public final class MethodFilter extends BaseFilter<DexBackedMethod> {
  private static final int M1 = -1;
  /** A {@code MethodFilter} instance that matches all dex methods. */
  public static final MethodFilter MATCH_ALL = new MethodFilter(builder());

  private final int flag;
  private final int skipFlag;
  private final int paramSize;
  private final String returnType;
  private final Set<String> methodNames;
  private final List<String> parameters;

  private MethodFilter(Builder builder) {
    super(builder, Utils.isSingle(builder.methodNames) &&
         (builder.parameters != null || builder.paramSize == 0));
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.paramSize = builder.paramSize;
    this.parameters = builder.parameters;
    this.returnType = builder.returnType;
    this.methodNames = builder.methodNames;
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexBackedMethod dexMethod,
                        @Nonnull LazyDecoder<DexBackedMethod> decoder) {
    if (this == MATCH_ALL) return true;
    if (!checkMethodSignature(dexMethod)) return false;
    boolean result = (
            (flag == M1 || (dexMethod.accessFlags & flag) == flag) &&
            (skipFlag == M1 || (dexMethod.accessFlags & skipFlag) == 0) &&
            (returnType == null || returnType.equals(dexMethod.getReturnType())) &&
            super.verify(dexMethod, decoder)
    );
    if (unique && !result) {
      throw new AbortException("Method found but the filter didn't match");
    }
    return result;
  }

  private boolean checkMethodSignature(DexBackedMethod dexMethod) {
    if (methodNames != null && !methodNames.contains(dexMethod.getName())) {
      return false;
    } else if (parameters != null) {
      List<String> list = dexMethod.getParameterTypes();
      return parameters.size() == list.size() && list.equals(parameters);
    }
    return paramSize < 0 || dexMethod.getParameterTypes().size() == paramSize;
  }

  public static MethodFilter ofMethod(@Nonnull String method,
                                      @Nonnull List<String> params) {
    return builder().setMethodNames(method).setParamList(params).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, MethodFilter> {
    private int flag = M1;
    private int skipFlag = M1;
    private int paramSize = M1;
    private String returnType;
    private Set<String> methodNames;
    private List<String> parameters;

    public Builder() {}

    private Builder(MethodFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.paramSize = instance.paramSize;
      this.parameters = instance.parameters;
      this.returnType = instance.returnType;
      this.methodNames = instance.methodNames;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault()  &&
             flag == M1         &&
             skipFlag == M1     &&
             paramSize == M1    &&
             parameters == null &&
             returnType == null &&
             methodNames == null;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public MethodFilter build() {
      return isDefault() ? MATCH_ALL : new MethodFilter(this);
    }

    public Builder setMethodNames(@Nonnull String... names) {
      List<String> list = Utils.nonNullList(names);
      this.methodNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Set method modifiers. eg: public, static, final etc...
     * @param modifiers see {@link java.lang.reflect.Modifier}
     *
     * @return this builder
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Methods with matching modifiers will be skipped
     * @param modifiers see {@link Builder#setModifiers(int)}
     *
     * @return this builder
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    public Builder setReturnType(@Nullable String returnType) {
      this.returnType = returnType == null ? null : DexUtils.javaToDexTypeName(returnType);
      return this;
    }

    public Builder setParamSize(int size) {
      this.paramSize = size;
      return this;
    }

    public Builder setParamList(@Nullable List<String> params) {
      this.parameters = params == null ? null : Utils.optimizedList(DexUtils.javaToDexTypeName(params));
      return this;
    }
  }
}
