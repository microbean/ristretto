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

import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestTypeSet {

  public TestTypeSet() {
    super();
  }

  @Test
  public void testTypeSet() {
    final TypeSet typeSet = new TypeSet(Integer.class);
    final Set<? extends Class<?>> classHierarchy = typeSet.getClassHierarchy();
    assertNotNull(classHierarchy);
    assertEquals(3, classHierarchy.size());
    assertTrue(classHierarchy.contains(Integer.class));
    assertTrue(classHierarchy.contains(Number.class));
    assertTrue(classHierarchy.contains(Object.class));
    final Set<? extends Class<?>> interfaceHierarchy = typeSet.getInterfaceHierarchy();
    assertNotNull(interfaceHierarchy);
    assertEquals(2, interfaceHierarchy.size());
    assertTrue(interfaceHierarchy.contains(Comparable.class));
    assertTrue(interfaceHierarchy.contains(Serializable.class));
  }

  @Test
  public void testTypeInfo() {
    final TypeInfo typeInfo = TypeInfo.of(Collections.singleton(Integer.class));
    assertNotNull(typeInfo);
    assertEquals(Integer.class, typeInfo.getSuperClass());
    assertNull(typeInfo.getSuperInterface());
  }

  // Harvested from Weld; for testing and understanding only
  public static class TypeInfo {

    private final Set<Class<?>> interfaces;
    private final Set<Class<?>> classes;

    private TypeInfo() {
      super();
      this.interfaces = new LinkedHashSet<Class<?>>();
      this.classes = new LinkedHashSet<Class<?>>();
    }

    public Class<?> getSuperClass() {
      if (classes.isEmpty()) {
        return Object.class;
      }
      Iterator<Class<?>> it = classes.iterator();
      Class<?> superclass = it.next();
      while (it.hasNext()) {
        Class<?> clazz = it.next();
        if (superclass.isAssignableFrom(clazz)) {
          superclass = clazz;
        }
      }
      return superclass;
    }

    public Class<?> getSuperInterface() {
      if (interfaces.isEmpty()) {
        return null;
      }
      Iterator<Class<?>> it = interfaces.iterator();
      Class<?> superclass = it.next();
      while (it.hasNext()) {
        Class<?> clazz = it.next();
        if (superclass.isAssignableFrom(clazz)) {
          superclass = clazz;
        }
      }
      return superclass;
    }

    private TypeInfo add(Type type) {
      if (type instanceof Class<?>) {
        Class<?> clazz = (Class<?>) type;
        if (clazz.isInterface()) {
          interfaces.add(clazz);
        } else {
          classes.add(clazz);
        }
      } else if (type instanceof ParameterizedType) {
        add(((ParameterizedType) type).getRawType());
      } else {
        throw new IllegalArgumentException();
      }
      return this;
    }

    public Set<Class<?>> getClasses() {
      return Collections.unmodifiableSet(classes);
    }

    public Set<Class<?>> getInterfaces() {
      return Collections.unmodifiableSet(interfaces);
    }

    public static TypeInfo of(Set<? extends Type> types) {
      TypeInfo typeInfo = new TypeInfo();
      for (Type type : types) {
        typeInfo.add(type);
      }
      return typeInfo;
    }

  }

  
}
