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

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

class FunctionalBean<T> extends FunctionalBeanAttributes<T> implements Bean<T> {

  private final Supplier<Class<?>> beanClassSupplier;
  
  private final Supplier<Set<InjectionPoint>> injectionPointsSupplier;

  private final BiConsumer<T, CreationalContext<T>> destroyer;

  private final Function<CreationalContext<T>, T> creator;

  FunctionalBean(final Bean<T> delegate) {
    this(delegate::getTypes,
         delegate::getQualifiers,
         delegate::getScope,
         delegate::getName,
         delegate::getStereotypes,
         delegate::isAlternative,
         delegate::getBeanClass,
         delegate::getInjectionPoints,
         delegate::destroy,
         delegate::create);
  }
  
  FunctionalBean(final Supplier<Set<Type>> typesSupplier,
                 final Supplier<Set<Annotation>> qualifiersSupplier,
                 final Supplier<Class<? extends Annotation>> scopeSupplier,
                 final Supplier<String> nameSupplier,
                 final Supplier<Set<Class<? extends Annotation>>> stereotypesSupplier,
                 final BooleanSupplier alternativeSupplier,
                 final Supplier<Class<?>> beanClassSupplier,
                 final Supplier<Set<InjectionPoint>> injectionPointsSupplier,
                 final BiConsumer<T, CreationalContext<T>> destroyer,
                 final Function<CreationalContext<T>, T> creator) {
    super(typesSupplier,
          qualifiersSupplier,
          scopeSupplier,
          nameSupplier,
          stereotypesSupplier,
          alternativeSupplier);
    this.beanClassSupplier = Objects.requireNonNull(beanClassSupplier);
    this.injectionPointsSupplier = injectionPointsSupplier;
    this.destroyer = destroyer;
    this.creator = Objects.requireNonNull(creator);
  }

  @Override
  public Class<?> getBeanClass() {
    return this.beanClassSupplier.get();
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return this.injectionPointsSupplier.get();
  }

  @Override
  public void destroy(final T instance, final CreationalContext<T> creationalContext) {
    this.destroyer.accept(instance, creationalContext);
  }

  @Override
  public T create(final CreationalContext<T> creationalContext) {
    return this.creator.apply(creationalContext);
  }  
  
}
