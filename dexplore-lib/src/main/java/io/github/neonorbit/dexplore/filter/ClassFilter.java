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
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ClassFilter extends BaseFilter<DexBackedClassDef> {
  private final int flag;
  private final int skipFlag;
  private final String superClass;
  private final Pattern pkgPattern;
  private final List<String> interfaces;
  private final Set<String> inClassNames;

  private final String className;
  private final boolean isUnique;
  private final boolean noInterface;

  private ClassFilter(Builder builder) {
    super(builder);
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.pkgPattern = builder.pkgPattern;
    this.superClass = builder.superClass;
    this.interfaces = builder.interfaces;
    this.inClassNames = builder.inClassNames;
    if (builder.inClassNames == null ||
        builder.inClassNames.size() != 1) {
      this.className = null;
    } else {
      this.className = this.inClassNames.iterator().next();
    }
    this.noInterface = this.interfaces != null &&
                       this.interfaces.isEmpty();
    this.isUnique = className != null;
  }

  public boolean isUnique() {
    return isUnique;
  }

  @Override
  public boolean verify(@Nonnull DexBackedClassDef dexClass,
                        @Nonnull LazyDecoder<DexBackedClassDef> decoder) {
    if (!checkClassName(dexClass)) return false;
    boolean result = (
            (flag == 0 || (dexClass.getAccessFlags() & flag) == flag) &&
            (skipFlag == 0 || (dexClass.getAccessFlags() & skipFlag) == 0) &&
            (superClass == null || superClass.equals(dexClass.getSuperclass())) &&
            (pkgPattern == null || pkgPattern.matcher(dexClass.getType()).matches()) &&
            checkInterface(dexClass) && super.verify(dexClass, decoder)
    );
    if (isUnique && !result) {
      throw new AbortException("Class found but the filter didn't match");
    }
    return result;
  }

  private boolean checkClassName(DexBackedClassDef dexClass) {
    if (className != null)
      return className.equals(dexClass.getType());
    if (inClassNames != null)
      return inClassNames.contains(dexClass.getType());
    return true;
  }

  private boolean checkInterface(DexBackedClassDef dexClass) {
    if (noInterface)
      return dexClass.getInterfaces().isEmpty();
    if (interfaces != null)
      return dexClass.getInterfaces().equals(interfaces);
    return true;
  }

  public static ClassFilter ofClass(@Nonnull String clazz) {
    Objects.requireNonNull(clazz);
    return builder().setClassNames(clazz).build();
  }

  public static ClassFilter none() {
    return builder().build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, ClassFilter> {
    private int flag;
    private int skipFlag;
    private Pattern pkgPattern;
    private String superClass;
    private List<String> interfaces;
    private Set<String> inClassNames;

    public Builder() {}

    private Builder(ClassFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.pkgPattern = instance.pkgPattern;
      this.superClass = instance.superClass;
      this.interfaces = instance.interfaces;
      this.inClassNames = instance.inClassNames;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public ClassFilter build() {
      return new ClassFilter(this);
    }

    /**
     * Specify a list of packages that should be excluded during the search operation
     * @param packages list of packages to exclude
     * @param exception an exception list to allow sub packages
     *
     * @return this builder
     */
    public Builder skipPackages(@Nullable List<String> packages,
                                @Nullable List<String> exception) {
      if (packages == null || packages.isEmpty()) {
        this.pkgPattern = null;
      } else if (!Utils.isValidName(packages) ||
                 !(exception == null || Utils.isValidName(exception))) {
        throw new InvalidParameterException("Invalid Package Name");
      } else {
        this.pkgPattern = getPattern(packages, exception);
      }
      return this;
    }

    public Builder setClassNames(@Nullable String... classes) {
      this.inClassNames = classes == null || classes.length == 0 ? null :
                          new HashSet<>(DexUtils.javaToDexTypeName(Arrays.asList(classes)));
      return this;
    }

    /**
     * Set class modifiers. eg: public, static, final etc...
     * @param modifiers see {@link java.lang.reflect.Modifier}
     *
     * @return this builder
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Classes with matching modifiers will be skipped
     * @param modifiers see {@link #setModifiers(int)}
     * @see #setModifiers(int)
     *
     * @return this builder
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    public Builder setSuperClass(@Nullable String superClass) {
      this.superClass = superClass == null ? null :
                        DexUtils.javaToDexTypeName(superClass);
      return this;
    }

    public Builder defaultSuperClass() {
      this.superClass = DexUtils.javaClassToDexTypeName(Object.class);
      return this;
    }

    public Builder setInterfaces(@Nullable String... interfaces) {
      this.interfaces = interfaces == null ? null :
                        interfaces.length == 0 ? Collections.emptyList() :
                        DexUtils.javaToDexTypeName(Arrays.asList(interfaces));
      return this;
    }

    public Builder noInterfaces() {
      return setInterfaces();
    }

    private static Pattern getPattern(List<String> packages, List<String> exception) {
      Function<String, String> mapper = s -> 'L' + s.replaceAll("\\.", "/")
                                                    .replaceAll("\\$", "\\\\\\$")
                                           + '/';
      String regex = "^((?!";
      regex += packages.stream().map(mapper).collect(Collectors.joining("|"));
      regex += ")";
      if (exception != null && !exception.isEmpty()) {
        regex += '|' + exception.stream().map(mapper).collect(Collectors.joining("|"));
      }
      regex += ").*$";
      return Pattern.compile(regex);
    }
  }
}
