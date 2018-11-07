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

import java.lang.invoke.MethodHandle;

import java.util.Objects;
import java.util.Set;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

class FunctionalProducer<T> implements Producer<T> {

  private final Function<CreationalContext<T>, T> producer;

  private final Consumer<T> disposer;

  private final Supplier<Set<InjectionPoint>> injectionPointsSupplier;

  FunctionalProducer(final MethodHandle producerMethodHandle,
                     final MethodHandle disposerMethodHandle,
                     final Supplier<Set<InjectionPoint>> injectionPointsSupplier) {
    this(cc -> {
        try {
          return (T)producerMethodHandle.invoke(cc);
        } catch (final RuntimeException | Error e) {
          throw e;
        } catch (final Throwable e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      },
      t -> {
        try {
          disposerMethodHandle.invoke(t);
        } catch (final RuntimeException | Error e) {
          throw e;
        } catch (final Throwable e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      },
      injectionPointsSupplier);
  }
  
  FunctionalProducer(final Function<CreationalContext<T>, T> producer,
                     final Consumer<T> disposer,
                     final Supplier<Set<InjectionPoint>> injectionPointsSupplier) {
    this.producer = Objects.requireNonNull(producer);
    this.disposer = Objects.requireNonNull(disposer);
    this.injectionPointsSupplier = Objects.requireNonNull(injectionPointsSupplier);
  }

  @Override
  public T produce(final CreationalContext<T> creationalContext) {
    return this.producer.apply(creationalContext);
  }

  @Override
  public void dispose(final T instance) {
    this.disposer.accept(instance);
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return this.injectionPointsSupplier.get();
  }
  
}
