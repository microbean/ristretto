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

import java.util.Map;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.spi.Contextual;

public final class MapBackedStorage implements Storage {

  private final AtomicBoolean destroyed;
  
  private final Map<Contextual<?>, ContextualInstance<?>> map;
  
  public MapBackedStorage(final Map<Contextual<?>, ContextualInstance<?>> map) {
    super();
    this.destroyed = new AtomicBoolean();
    this.map = Objects.requireNonNull(map);
  }

  @Override
  public final boolean destroy() {
    if (this.destroyed.getAndSet(true)) {
      throw new IllegalStateException();
    }
    this.map.forEach((k, v) -> v.destroy());
    this.map.clear();
    return true;
  }

  @Override
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }

  @Override
  public final void forEach(final Consumer<? super ContextualInstance<?>> consumer) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    this.map.forEach((k, v) -> consumer.accept(v));
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <T> ContextualInstance<T> get(final Contextual<T> key) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    return (ContextualInstance<T>)this.map.get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <T> ContextualInstance<T> computeIfAbsent(final Contextual<T> key, final Function<? super Contextual<T>, ? extends ContextualInstance<T>> mappingFunction) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    return (ContextualInstance<T>)this.map.computeIfAbsent(key, (Function<? super Contextual<?>, ? extends ContextualInstance<?>>)mappingFunction);
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <T> ContextualInstance<T> remove(final Contextual<T> key) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    return (ContextualInstance<T>)this.map.remove(key);
  }
  
}
