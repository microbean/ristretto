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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.atomic.AtomicReference;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.enterprise.context.ContextNotActiveException;

import org.microbean.ristretto.context.ContextualInstance;

public class ContextualReference<T> implements Supplier<T> {

  private final AtomicReference<T> proxy;

  private final Set<? extends Type> beanTypes;
  
  private final ContextualInstance<T> contextualInstance;

  private final BooleanSupplier activeSupplier;

  private final BiFunction<Supplier<T>, Set<? extends Type>, T> proxyCreator;

  public ContextualReference(final ContextualInstance<T> contextualInstance,
                             final Type beanType,
                             final BiFunction<Supplier<T>, Set<? extends Type>, T> proxyCreator) {
    this(contextualInstance, beanType, contextualInstance::isActive, proxyCreator);
  }
  
  public ContextualReference(final ContextualInstance<T> contextualInstance,
                             final Type beanType,
                             final BooleanSupplier activeSupplier,
                             final BiFunction<Supplier<T>, Set<? extends Type>, T> proxyCreator) {
    super();
    this.contextualInstance = Objects.requireNonNull(contextualInstance);
    Objects.requireNonNull(beanType);
    this.proxyCreator = Objects.requireNonNull(proxyCreator);
    this.beanTypes = Collections.singleton(Objects.requireNonNull(beanType));
    this.proxy = new AtomicReference<>();
    this.activeSupplier = activeSupplier == null ? contextualInstance::isActive : activeSupplier;
  }
  
  private final ContextualInstance<T> getContextualInstance() {
    return this.contextualInstance;
  }

  public final boolean isActive() {
    return this.activeSupplier.getAsBoolean();
  }

  /**
   * @exception ContextNotActiveException
   *
   * @exception UnproxyableResolutionException
   */
  @Override
  public final T get() {
    if (!this.isActive()) {
      throw new ContextNotActiveException();
    }
    final ContextualInstance<T> contextualInstance = this.getContextualInstance();
    assert contextualInstance != null;
    final T returnValue;
    if (contextualInstance.isNormal()) {
      returnValue = this.proxy(contextualInstance);
    } else {
      returnValue = contextualInstance.get();
    }
    return returnValue;
  }

  private final T proxy(final Supplier<T> contextualInstance) {
    return this.proxy.updateAndGet(t -> t == null ? this.proxyCreator.apply(contextualInstance, this.beanTypes) : t);
  }
  
}
