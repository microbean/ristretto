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

import java.util.Objects;
import java.util.Set;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Producer;

class FunctionalInjectionTarget<T> extends FunctionalProducer<T> implements InjectionTarget<T> {

  private final Consumer<T> postConstructor;
  
  private final BiConsumer<T, CreationalContext<T>> injector;

  private final Consumer<T> preDestroyer;

  FunctionalInjectionTarget(final Producer<T> producer,
                            final BiConsumer<T, CreationalContext<T>> injector,
                            final Consumer<T> postConstructor,
                            final Consumer<T> preDestroyer) {
    this(producer::produce, producer::dispose, producer::getInjectionPoints, injector, postConstructor, preDestroyer);
  }
  
  FunctionalInjectionTarget(final Function<CreationalContext<T>, T> producer,
                            final Consumer<T> disposer,
                            final Supplier<Set<InjectionPoint>> injectionPointsSupplier,
                            final BiConsumer<T, CreationalContext<T>> injector,
                            final Consumer<T> postConstructor,
                            final Consumer<T> preDestroyer) {
    super(producer, disposer, injectionPointsSupplier);
    this.injector = Objects.requireNonNull(injector);
    this.postConstructor = Objects.requireNonNull(postConstructor);
    this.preDestroyer = Objects.requireNonNull(preDestroyer);
  }

  @Override
  public void postConstruct(final T instance) {
    this.postConstructor.accept(instance);
  }
  
  @Override
  public void inject(final T instance, final CreationalContext<T> creationalContext) {
    this.injector.accept(instance, creationalContext);
  }

  @Override
  public void preDestroy(final T instance) {
    this.preDestroyer.accept(instance);
  }
  
}
