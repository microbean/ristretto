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

import javax.enterprise.context.ContextNotActiveException;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public final class ContextualInstance<T> implements Destroyable, Supplier<T> {

  private final AtomicBoolean destroyed;

  private Context context;
  
  private Contextual<T> contextual;
  
  private CreationalContext<T> creationalContext;

  private final boolean normal;

  ContextualInstance(final Context context,
                     final Contextual<T> contextual,
                     final CreationalContext<T> creationalContext) {
    super();
    this.destroyed = new AtomicBoolean();
    this.context = Objects.requireNonNull(context);
    this.contextual = Objects.requireNonNull(contextual);
    this.creationalContext = creationalContext;
    this.normal = Contexts.isNormal(context);
  }

  @Override
  public final T get() {
    final T returnValue;
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    } else if (!this.isActive()) {
      throw new ContextNotActiveException();
    } else {
      returnValue = this.context.get(this.contextual, this.creationalContext);
    }
    return returnValue;
  }
  
  public final boolean isActive() {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    return this.context.isActive();
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
      if (this.context instanceof AlterableContext) {
        ((AlterableContext)this.context).destroy(this.contextual);
      } else {
        final T instance = this.context.get(this.contextual, null);
        if (instance != null) {
          this.contextual.destroy(instance,
                                  new CreationalContext<>() {
                                    @Override
                                    public final void push(final T incompleteInstance) {
                                      throw new IllegalStateException();
                                    }
                                    
                                    @Override
                                    public final void release() {
                                      final CreationalContext<T> cc = ContextualInstance.this.creationalContext;
                                      if (cc != null) {
                                        cc.release();
                                      }
                                    }
                                  });
        }
      }
      this.context = null;
      this.contextual = null;
      this.creationalContext = null;
      returnValue = true;
    } else {
      returnValue = false;
    }
    return returnValue;
  }

}
