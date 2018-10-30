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

import java.util.function.Predicate;

import javax.enterprise.context.spi.Contextual;

class CreationalContext<T> implements DependentInstanceCollection<T> {

  private final Deque<T> incompleteInstances;
  
  private final Contextual<T> contextual;

  private final Collection<T> dependentInstances;

  CreationalContext(final Contextual<T> contextual) {
    super();
    this.incompleteInstances = new ArrayDeque<>();
    this.dependentInstances = new ArrayList<>();
    this.contextual = contextual;
  }

  @Override
  public void push(final T incompleteInstance) {
    if (incompleteInstance != null) {
      synchronized (this.incompleteInstances) {
        this.incompleteInstances.addFirst(incompleteInstance);
      }
    }
  }

  @Override
  public void release() {
    this.forEachDependentInstance(t -> {
        this.contextual.destroy(t, this);
        return true; // yes, remove the reference
      });
  }


  /*
   * DependentInstanceCollection implementation.
   */
  

  @Override
  public void addDependentInstance(final T dependentInstance) {
    synchronized (this.dependentInstances) {
      this.dependentInstances.add(dependentInstance);
    }
  }

  @Override
  public void forEachDependentInstance(final Predicate<? super T> function) {
    if (function != null) {
      synchronized (this.dependentInstances) {
        final Iterator<? extends T> iterator = this.dependentInstances.iterator();
        while (iterator.hasNext()) {
          final T instance = iterator.next();
          if (function.test(instance)) {
            iterator.remove();
          }
        }
      }
    }
  }
  
}