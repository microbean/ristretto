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

import java.lang.annotation.Annotation;

import java.lang.reflect.Type;

import java.util.Objects;
import java.util.Set;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.enterprise.inject.spi.BeanAttributes;

class FunctionalBeanAttributes<T> implements BeanAttributes<T> {

  private final Supplier<Set<Type>> typesSupplier;

  private final Supplier<Set<Annotation>> qualifiersSupplier;

  private final Supplier<Class<? extends Annotation>> scopeSupplier;

  private final Supplier<String> nameSupplier;

  private final Supplier<Set<Class<? extends Annotation>>> stereotypesSupplier;

  private final BooleanSupplier alternativeSupplier;

  FunctionalBeanAttributes(final BeanAttributes<T> delegate) {
    this(delegate::getTypes, delegate::getQualifiers, delegate::getScope, delegate::getName, delegate::getStereotypes, delegate::isAlternative);
  }
  
  FunctionalBeanAttributes(final Supplier<Set<Type>> typesSupplier,
                           final Supplier<Set<Annotation>> qualifiersSupplier,
                           final Supplier<Class<? extends Annotation>> scopeSupplier) {
    this(typesSupplier, qualifiersSupplier, scopeSupplier, null, null, null);
  }
  
  FunctionalBeanAttributes(final Supplier<Set<Type>> typesSupplier,
                           final Supplier<Set<Annotation>> qualifiersSupplier,
                           final Supplier<Class<? extends Annotation>> scopeSupplier,
                           final Supplier<String> nameSupplier,
                           final Supplier<Set<Class<? extends Annotation>>> stereotypesSupplier,
                           final BooleanSupplier alternativeSupplier) {
    this.typesSupplier = Objects.requireNonNull(typesSupplier);
    this.qualifiersSupplier = Objects.requireNonNull(qualifiersSupplier);
    this.scopeSupplier = Objects.requireNonNull(scopeSupplier);
    this.nameSupplier = nameSupplier;
    this.stereotypesSupplier = stereotypesSupplier;
    this.alternativeSupplier = alternativeSupplier;
  }

  @Override
  public Set<Type> getTypes() {
    return this.typesSupplier.get();
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return this.qualifiersSupplier.get();
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return this.scopeSupplier.get();
  }

  @Override
  public String getName() {
    return this.nameSupplier == null ? null : this.nameSupplier.get();
  }

  @Override
  public Set<Class<? extends Annotation>> getStereotypes() {
    return this.stereotypesSupplier == null ? null : this.stereotypesSupplier.get();
  }

  @Override
  public boolean isAlternative() {
    return this.alternativeSupplier == null ? false : this.alternativeSupplier.getAsBoolean();
  }
  
}
