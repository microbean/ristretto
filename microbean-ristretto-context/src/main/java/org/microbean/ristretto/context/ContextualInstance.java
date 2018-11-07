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
package org.microbean.ristretto.context;

import java.util.Objects;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.enterprise.context.ContextNotActiveException;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public final class ContextualInstance<T> implements Destroyable, Supplier<T> {

  private final AtomicBoolean destroyed;
  
  private BiConsumer<T, CreationalContext<T>> destroyer;

  private T instance;

  private final BooleanSupplier activeSupplier;
  
  private CreationalContext<T> creationalContext;

  private final boolean normal;

  ContextualInstance(final T instance,
                     final boolean normal) {
    this(instance, null, null, () -> true, normal);
  }
  
  ContextualInstance(final T instance,
                     final boolean active,
                     final boolean normal) {
    this(instance, null, null, () -> active, normal);
  }
  
  ContextualInstance(final T instance,
                     final BooleanSupplier activeSupplier,
                     final boolean normal) {
    this(instance, null, null, activeSupplier, normal);
  }
  
  ContextualInstance(final T instance,
                     final BiConsumer<T, CreationalContext<T>> destroyer,
                     final CreationalContext<T> creationalContext,
                     final BooleanSupplier activeSupplier,
                     final boolean normal) {
    super();
    this.destroyed = new AtomicBoolean();
    this.instance = instance;
    this.destroyer = destroyer;
    this.creationalContext = Objects.requireNonNull(creationalContext);
    this.activeSupplier = activeSupplier == null ? () -> true : activeSupplier;
    this.normal = normal;
  }

  @Override
  public final T get() {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    } else if (!this.isActive()) {
      throw new ContextNotActiveException();
    }
    return this.instance;
  }

  public final boolean isActive() {
    return !this.isDestroyed() && this.activeSupplier.getAsBoolean();
  }
  
  @Override
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }

  public final boolean isNormal() {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    return this.normal;
  }
  
  @Override
  public final boolean destroy() {
    final boolean returnValue;
    if (this.destroyed.compareAndSet(false, true)) {
      if (this.destroyer != null) {
        this.destroyer.accept(this.instance, this.creationalContext);
      }
      this.instance = null;
      this.creationalContext = null;
      this.destroyer = null;      
      returnValue = true;
    } else {
      returnValue = false;
    }
    return returnValue;
  }
  
}
