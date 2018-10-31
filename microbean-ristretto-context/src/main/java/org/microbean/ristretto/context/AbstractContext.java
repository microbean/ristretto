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

abstract class AbstractContext implements AlterableContext {

  private final AtomicBoolean active;
  
  private final Class<? extends Annotation> scope;

  private final Storage storage;

  AbstractContext(final Class<? extends Annotation> scope) {
    this(scope, new MapBackedStorage(new ConcurrentHashMap<>()));
  }
  
  AbstractContext(final Class<? extends Annotation> scope, final Storage storage) {
    super();
    this.active = new AtomicBoolean(true);
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
        final Supplier<T> contextualInstance = storage.get(contextual);
        if (contextualInstance == null) {
          returnValue = null;
        } else {
          returnValue = contextualInstance.get();
        }
      }
    } else if (this.storage == null) {
      returnValue = contextual.create(creationalContext);
    } else {
      final Supplier<T> contextualInstance = this.storage.computeIfAbsent(contextual, c -> new ContextualInstance<T>(c, c.create(creationalContext), creationalContext));
      assert contextualInstance != null;
      returnValue = contextualInstance.get();
    }
    return returnValue;
  }

  @Override
  public void destroy(final Contextual<?> contextual) {
    if (!this.isActive()) {
      throw new ContextNotActiveException();
    }
    if (contextual != null && this.storage != null) {
      final Destroyable contextualInstance = this.storage.remove(contextual);
      if (contextualInstance != null) {
        contextualInstance.destroy();
      }
    }
  }

  @Override
  public boolean isActive() {
    return this.active.get();
  }

  protected void setActive(final boolean active) {
    this.active.set(active);
  }    

  public void destroy() {
    
  }
  
  @Override
  public final Class<? extends Annotation> getScope() {
    return this.scope;
  }
  
}
