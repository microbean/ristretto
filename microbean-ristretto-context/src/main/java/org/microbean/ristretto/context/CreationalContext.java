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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Predicate;

import javax.enterprise.context.spi.Contextual;

final class CreationalContext<T> implements javax.enterprise.context.spi.CreationalContext<T>, DependentInstanceCollection<T> {

  private final AtomicBoolean released;
  
  private final Deque<ContextualInstance<T>> incompleteInstances;
  
  private final Contextual<T> contextual;

  private final Collection<ContextualInstance<? extends T>> dependentInstances;

  CreationalContext(final Contextual<T> contextual) {
    super();
    this.released = new AtomicBoolean(false);
    this.incompleteInstances = new ArrayDeque<>();
    this.dependentInstances = new ArrayList<>();
    this.contextual = contextual;
  }

  @Override
  public final void push(final T incompleteInstance) {
    if (this.released.get()) {
      throw new IllegalStateException();
    }
    if (incompleteInstance != null) {      
      synchronized (this.incompleteInstances) {
        // TODO: revisit; are pushed incomplete instances contextual instances or just "normal" instances?
        this.incompleteInstances.addFirst(new ContextualInstance<>(incompleteInstance, this.contextual::destroy, this, () -> true, false));
      }
    }
  }

  @Override
  public final void release() {
    if (this.released.getAndSet(true)) {
      throw new IllegalStateException();
    }
    this.removeDependentInstanceIf(contextualInstance -> contextualInstance.destroy());
    synchronized (this.incompleteInstances) {
      this.incompleteInstances.clear();
    }
  }


  /*
   * DependentInstanceCollection implementation.
   */
  

  @Override
  public final void addDependentInstance(final ContextualInstance<? extends T> dependentInstance) {
    if (this.released.get()) {
      throw new IllegalStateException();
    }
    synchronized (this.dependentInstances) {
      this.dependentInstances.add(dependentInstance);
    }
  }

  @Override
  public final void removeDependentInstanceIf(final Predicate<? super ContextualInstance<? extends T>> function) {
    if (this.released.get()) {
      throw new IllegalStateException();
    }
    if (function != null) {
      synchronized (this.dependentInstances) {
        this.dependentInstances.removeIf(function);
      }
    }
  }
  
}
