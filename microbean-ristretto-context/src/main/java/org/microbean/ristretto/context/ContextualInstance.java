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

import java.util.function.Supplier;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public final class ContextualInstance<T> implements Destroyable, Supplier<T> {

  private final AtomicBoolean destroyed;
  
  private final T instance;

  private final Contextual<T> contextual;

  private final CreationalContext<T> creationalContext;
  
  public ContextualInstance(final Contextual<T> contextual, final T instance, final CreationalContext<T> creationalContext) {
    super();
    this.destroyed = new AtomicBoolean();
    this.contextual = Objects.requireNonNull(contextual);
    this.creationalContext = creationalContext;
    this.instance = instance;
  }

  @Override
  public final T get() {
    if (this.destroyed.get()) {
      throw new IllegalStateException();
    }
    return this.instance;
  }

  @Override
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }

  @Override
  public final boolean destroy() {
    final boolean returnValue;
    if (this.destroyed.compareAndSet(false, true)) {
      this.contextual.destroy(this.instance, this.creationalContext);
      returnValue = true;
    } else {
      returnValue = false;
    }
    return returnValue;
  }
  
}
