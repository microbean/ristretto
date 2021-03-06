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

import java.util.Collection;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;

public interface DependentInstanceCollection<T> {

  /**
   * Adds an instance as a dependency of this {@link
   * DependentInstanceCollection} implementation together with an
   * optional means of destroying it later.
   *
   * @param dependentInstance the instance to add; must not be {@code
   * null}
   *
   * @param destroyer a {@link Consumer} whose {@link
   * Consumer#accept(Object)} method will destroy the instance when
   * necessary
   */
  void addDependentInstance(final T dependentInstance, final Consumer<? super T> destroyer);

  /**
   * 
   */
  void removeDependentInstances(final Predicate<? super Destroyable> predicate);
  
}
