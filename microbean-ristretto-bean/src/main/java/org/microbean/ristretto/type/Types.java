/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2018 microBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.ristretto.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import java.util.function.Predicate;

/**
 * A class providing {@link Type}-related methods.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class Types {

  /**
   * Creates a new {@link Types}.
   */
  private Types() {
    super();
  }

  public static final Class<?> toClass(final Type type) {
    return toClass(type, null);
  }

  public static final Class<?> toClass(final Type type, final Predicate<? super Type> predicate) {
    final Class<?> returnValue;
    if (type == null) {
      returnValue = Object.class; // I guess
    } else if (type instanceof Class) {
      returnValue = (Class<?>)type;
    } else if (predicate == null || predicate.test(type)) {
      if (type instanceof ParameterizedType) {
        returnValue = toClass(((ParameterizedType)type).getRawType(), predicate);
      } else if (type instanceof GenericArrayType) {
        returnValue = toClass(((GenericArrayType)type).getGenericComponentType(), predicate);
      } else if (type instanceof WildcardType) {
        returnValue = toClass(((WildcardType)type).getUpperBounds(), predicate);
      } else if (type instanceof TypeVariable) {
        returnValue = toClass(((TypeVariable<?>)type).getBounds(), predicate);
      } else {
        throw new IllegalArgumentException(type.toString());
      }
    } else {
      throw new IllegalArgumentException(type.toString());
    }
    return returnValue;
  }

  public static final Set<? extends Class<?>> toClasses(final Set<? extends Type> types) {
    return toClasses(types, null);
  }
  
  public static final Set<? extends Class<?>> toClasses(final Set<? extends Type> types, final Predicate<? super Type> predicate) {
    final Set<? extends Class<?>> returnValue;
    if (types == null || types.isEmpty()) {
      returnValue = Collections.singleton(Object.class);
    } else {
      final Set<Class<?>> classes = new LinkedHashSet<>();
      for (final Type type : types) {
        classes.add(toClass(type, predicate));
      }
      assert !classes.isEmpty();
      returnValue = Collections.unmodifiableSet(classes);
    }
    return returnValue;
  }

  private static final Class<?> toClass(final Type[] types, final Predicate<? super Type> predicate) {
    final Class<?> returnValue;
    if (types == null || types.length <= 0) {
      returnValue = Object.class;
    } else {
      returnValue = toClass(types[0], predicate);
    }
    return returnValue;
  }
  
}
