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

import java.lang.reflect.Type;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import java.util.function.Supplier;

public final class TypeSet {

  private final Set<? extends Class<?>> classes;

  private final Set<? extends Class<?>> interfaces;

  public TypeSet(final Type type) {
    this(Collections.singleton(type));
  }

  public TypeSet(final Set<? extends Type> suppliedTypes) {
    super();
    if (suppliedTypes == null || suppliedTypes.isEmpty()) {      
      this.classes = Collections.singleton(Object.class);
      this.interfaces = Collections.emptySet();
    } else {
      final Set<Class<?>> typesToProcess = new LinkedHashSet<>();
      for (final Type suppliedType : suppliedTypes) {
        if (suppliedType != null) {
          typesToProcess.add(Types.toClass(suppliedType));
        }
      }
      final Set<Class<?>> classes = new LinkedHashSet<>(7);
      final Set<Class<?>> interfaces = new LinkedHashSet<>(7);
      while (!typesToProcess.isEmpty()) {
        final Iterator<Class<?>> iterator = typesToProcess.iterator();
        final Class<?> c = iterator.next();
        iterator.remove();        
        if (c.isInterface()) {
          interfaces.add(c);
        } else {
          classes.add(c);
        }
        if (!Object.class.equals(c)) {
          final Type genericSuperclass = c.getGenericSuperclass();
          if (genericSuperclass != null) {
            typesToProcess.add(Types.toClass(genericSuperclass));
          }
          final Type[] genericInterfaces = c.getGenericInterfaces();
          if (genericInterfaces != null && genericInterfaces.length > 0) {
            for (final Type genericInterface : genericInterfaces) {
              if (genericInterface != null) {
                typesToProcess.add(Types.toClass(genericInterface));
              }
            }
          }
        }
      }
      this.classes = Collections.unmodifiableSet(classes);
      this.interfaces = Collections.unmodifiableSet(interfaces);
    }
    assert !this.classes.isEmpty();
  }

  final Set<? extends Class<?>> getClassHierarchy() {
    return this.classes;
  }

  final Set<? extends Class<?>> getInterfaceHierarchy() {
    return this.interfaces;
  }

  // See
  // https://github.com/weld/core/blob/6c2b09fac4e694a20877f017424acd6c4b3e3439/impl/src/main/java/org/jboss/weld/bean/proxy/ProxyFactory.java#L174-L180
  public final Class<?> getSuperclass() {
    final Class<?> returnValue;
    final Set<? extends Class<?>> classHierarchy = this.getClassHierarchy();
    assert classHierarchy != null;
    assert !classHierarchy.isEmpty();
    if (classHierarchy.size() == 1) {
      assert classHierarchy.contains(Object.class);
      final Set<? extends Class<?>> interfaceHierarchy = this.getInterfaceHierarchy();
      if (interfaceHierarchy == null || interfaceHierarchy.isEmpty()) {
        returnValue = Object.class;
      } else {
        returnValue = interfaceHierarchy.iterator().next();
      }
      assert returnValue != null;
    } else {
      assert classHierarchy.size() > 1;
      returnValue = classHierarchy.iterator().next();
      assert returnValue != null;
    }
    return returnValue;
  }


  /*
   * Static methods.
   */

  
  private static final int compare(final Type one, final Type two) {
    final int returnValue;
    if (one == null) {
      if (two == null) {
        returnValue = 0;
      } else {
        returnValue = 1; // nulls sort to right
      }
    } else if (two == null) {
      returnValue = -1; // nulls sort to right
    } else if (one.equals(two)) {
      returnValue = 0;
    } else {
      final Class<?> c1 = Types.toClass(one);
      assert c1 != null;
      final Class<?> c2 = Types.toClass(two);
      assert c2 != null;
      if (Object.class.equals(c1)) {
        returnValue = 1;
      } else if (Object.class.equals(c2)) {
        returnValue = -1;
      } else if (c1.isAssignableFrom(c2)) {
        // e.g. c1 is Number.class; c2 is Integer.class
        // superclasses sort right
        returnValue = 1;
      } else if (c2.isAssignableFrom(c1)) {
        // e.g. c1 is Integer.class; c2 is Number.class
        // superclasses sort right
        returnValue = -1;
      } else {
        returnValue = 0;
      }
    }
    return returnValue;
  }
  
}
