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

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.enterprise.context.spi.Contextual;

final class CreationalContext<T> implements javax.enterprise.context.spi.CreationalContext<T>, DependentInstanceCollection<T> {

  private final AtomicBoolean released;
  
  private final Deque<T> incompleteInstances;
  
  private final Contextual<T> contextual;

  private final Collection<DestroyableSupplier<?>> dependentInstances;

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
        this.incompleteInstances.addFirst(incompleteInstance);
      }
    }
  }

  @Override
  public final void release() {
    if (this.released.getAndSet(true)) {
      throw new IllegalStateException();
    }
    this.removeDependentInstances(contextualInstance -> contextualInstance.destroy());
    synchronized (this.incompleteInstances) {
      this.incompleteInstances.clear();
    }
  }


  /*
   * DependentInstanceCollection implementation.
   */
  

  @Override
  public final void addDependentInstance(final T dependentInstance, final Consumer<? super T> destroyer) {
    if (this.released.get()) {
      throw new IllegalStateException();
    }
    synchronized (this.dependentInstances) {
      final DestroyableSupplier<T> destroyableSupplier = new DestroyableSupplier<>() {
          @Override
          public final T get() {
            return dependentInstance;
          }

          @Override
          protected boolean performDestruction() {
            if (destroyer != null) {
              destroyer.accept(dependentInstance);
              return true;
            }
            return false;
          }
        };
      this.dependentInstances.add(destroyableSupplier);
    }
  }

  @Override
  public final void removeDependentInstances(final Predicate<? super Destroyable> predicate) {
    if (this.released.get()) {
      throw new IllegalStateException();
    }
    if (predicate != null) {
      synchronized (this.dependentInstances) {
        this.dependentInstances.removeIf(predicate);
      }
    }
  }
  
}
