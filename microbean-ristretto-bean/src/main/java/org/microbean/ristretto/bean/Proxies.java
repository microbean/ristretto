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
package org.microbean.ristretto.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.UnproxyableResolutionException;

import org.microbean.ristretto.type.Types;

public final class Proxies {

  public static final boolean canProxy(final Object bean, final Type type, final boolean ignoreFinalMethods) throws ReflectiveOperationException {
    return canProxy(bean, Collections.singleton(type), ignoreFinalMethods);
  }
  
  // See http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#unproxyable
  public static final boolean canProxy(final Object bean, final Set<? extends Type> types, final boolean ignoreFinalMethods) throws ReflectiveOperationException {
    if (types == null) {
      throw new UnproxyableResolutionException(new IllegalArgumentException("types == null"));
    } else if (types.isEmpty()) {
      throw new UnproxyableResolutionException(new IllegalArgumentException("types.isEmpty()"));
    } else {
      for (final Type type : types) {
        if (type == null) {
          throw new UnproxyableResolutionException(new IllegalArgumentException("type == null"));
        } else if (type instanceof TypeVariable) {
          throw new UnproxyableResolutionException("A TypeVariable cannot be proxied");
        } else if (type instanceof WildcardType) {
          throw new UnproxyableResolutionException("A WildcardType cannot be proxied");
        } else {
          final Class<?> cls = Types.toClass(type);
          assert cls != null;
          if (cls.isPrimitive()) {
            throw new UnproxyableResolutionException("A primitive Class cannot be proxied");
          } else if (cls.isArray()) {
            throw new UnproxyableResolutionException("An array-typed Class cannot be proxied");
          } else {
            final int modifiers = cls.getModifiers();
            if (Modifier.isFinal(modifiers)) {
              throw new UnproxyableResolutionException("A final Class cannot be proxied");
            } else if (!ignoreFinalMethods) {
              Class<?> c = cls;
              while (c != null && !c.equals(Object.class)) {
                final Method[] declaredMethods = c.getDeclaredMethods();
                assert declaredMethods != null;
                for (final Method m : declaredMethods) {
                  assert m != null;
                  final int methodModifiers = m.getModifiers();
                  if (Modifier.isFinal(methodModifiers) &&
                      !Modifier.isPrivate(methodModifiers) &&
                      !Modifier.isStatic(methodModifiers)) {
                    // Non-private (public, protected or package-private)
                    // final instance method
                    throw new UnproxyableResolutionException(m.toString());
                  }
                }
                c = c.getSuperclass();
              }
              c = null;
            } else if (!cls.isInterface()) {
              final Constructor<?> noArgumentConstructor = cls.getDeclaredConstructor();
              assert noArgumentConstructor != null;
              if (Modifier.isPrivate(noArgumentConstructor.getModifiers())) {
                throw new UnproxyableResolutionException("A Class with a private zero-argument constructor cannot be proxied");
              }
            }
          }
        }
      }
    }
    return true;
  }
    
}
