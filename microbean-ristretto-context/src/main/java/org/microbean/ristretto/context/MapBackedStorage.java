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

import java.util.function.Supplier;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.spi.Contextual;

public final class MapBackedStorage implements Storage {

  private final AtomicBoolean destroyed;
  
  private final Map<Object, DestroyableSupplier<?>> map;
  
  public MapBackedStorage(final Map<Object, DestroyableSupplier<?>> map) {
    super();
    this.destroyed = new AtomicBoolean();
    this.map = Objects.requireNonNull(map);
  }

  @Override // Destroyable
  public final boolean destroy() {
    if (this.destroyed.getAndSet(true)) {
      throw new IllegalStateException();
    }
    this.map.forEach((k, v) -> v.destroy());
    this.map.clear();
    return true;
  }

  @Override // Destroyable
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }

  @Override // Storage
  public final void forEach(final Consumer<? super Destroyable> consumer) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    this.map.forEach((k, v) -> consumer.accept(v));
  }

  @Override // Storage
  public final <T> T get(final Object key) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    final T returnValue;
    final Supplier<?> supplier = this.map.get(key);
    if (supplier == null) {
      returnValue = null;
    } else {
      @SuppressWarnings("unchecked")
      final T temp = (T)supplier.get();
      returnValue = temp;
    }
    return returnValue;
  }

  @Override // Storage
  public final <T> DestroyableSupplier<? extends T> computeIfAbsent(final Object key, final Function<? super Object, ? extends DestroyableSupplier<? extends T>> mappingFunction) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    @SuppressWarnings("unchecked")
    final DestroyableSupplier<? extends T> returnValue = (DestroyableSupplier<? extends T>)this.map.computeIfAbsent(key, mappingFunction);
    return returnValue;
  }

  @Override // Storage
  public final <T> DestroyableSupplier<? extends T> remove(final Object key) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    @SuppressWarnings("unchecked")
    final DestroyableSupplier<? extends T> returnValue = (DestroyableSupplier<? extends T>)this.map.remove(key);
    return returnValue;
  }
  
}
