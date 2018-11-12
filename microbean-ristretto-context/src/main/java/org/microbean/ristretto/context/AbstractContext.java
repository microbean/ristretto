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

import java.lang.annotation.Annotation;

import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Supplier;

import javax.enterprise.context.ContextNotActiveException;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

class AbstractContext implements AlterableContext, Destroyable {

  private final AtomicBoolean active;

  private final AtomicBoolean destroyed;
  
  private final Class<? extends Annotation> scope;

  private final Storage storage;

  AbstractContext(final Class<? extends Annotation> scope) {
    this(scope, true, new MapBackedStorage(new ConcurrentHashMap<>()));
  }

  AbstractContext(final Class<? extends Annotation> scope, final Storage storage) {
    this(scope, true, storage);
  }
  
  AbstractContext(final Class<? extends Annotation> scope, final boolean active, final Storage storage) {
    super();
    this.active = new AtomicBoolean(active);
    this.destroyed = new AtomicBoolean(false);
    this.scope = Objects.requireNonNull(scope);
    this.storage = storage;
  }

  @Override
  public final <T> T get(final Contextual<T> contextual) {
    return this.get(contextual, null);
  }

  @Override
  public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
    Objects.requireNonNull(contextual);
    if (!this.isActive()) {
      throw new ContextNotActiveException();
    }
    final T returnValue;
    if (creationalContext == null) {
      if (this.storage == null) {        
        returnValue = null;
      } else {
        returnValue = this.storage.get(contextual);
      }
    } else if (this.storage == null) {
      returnValue = contextual.create(creationalContext);
    } else {
      final DestroyableSupplier<? extends T> instance =
        this.storage.computeIfAbsent(contextual,
                                     k -> computeIfAbsent(contextual, creationalContext));
      assert instance != null;
      returnValue = instance.get();
    }
    return returnValue;
  }

  private static final <T> DestroyableSupplier<? extends T> computeIfAbsent(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
    final T instance = contextual.create(creationalContext);
    return new DestroyableSupplier<T>() {
      @Override
      public final T get() {
        return instance;
      }
      
      @Override
      protected final boolean performDestruction() {
        contextual.destroy(instance, creationalContext);
        return true;
      }
    };
  }

  @Override // AlterableContext
  public final void destroy(final Contextual<?> contextual) {
    if (!this.isActive()) {
      throw new ContextNotActiveException();
    }
    if (contextual != null && this.storage != null) {
      final Destroyable instance = this.storage.remove(contextual);
      if (instance != null) {
        instance.destroy();
      }
    }
  }

  @Override // Context
  public final boolean isActive() {
    return this.active.get();
  }

  protected final void setActive(final boolean active) {
    if (active && this.isDestroyed()) {
      throw new IllegalStateException();
    }
    this.active.set(active);
  }

  @Override
  public final Class<? extends Annotation> getScope() {
    return this.scope;
  }
  
  @Override
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }
  
  @Override
  public final boolean destroy() {
    this.setActive(false);
    this.destroyed.set(true);
    final boolean returnValue = this.storage == null ? true : this.storage.destroy();
    return returnValue;
  }
  
}
