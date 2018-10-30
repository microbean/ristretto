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

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

final class DependentContext implements AlterableContext {

  DependentContext() {
    super();
  }

  @Override
  public final <T> T get(final Contextual<T> contextual) {
    return get(contextual, null);
  }
  
  @Override
  public final <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
    Objects.requireNonNull(contextual);
    final T returnValue;
    if (!this.isActive()) {
      throw new ContextNotActiveException();
    } else if (creationalContext == null) {
      returnValue = null;
    } else {
      returnValue = contextual.create(creationalContext);
      if (returnValue != null && creationalContext instanceof DependentInstanceCollection) {
        @SuppressWarnings("unchecked")
        final DependentInstanceCollection<T> dependentInstanceCollection = (DependentInstanceCollection<T>)creationalContext;
        dependentInstanceCollection.addDependentInstance(returnValue);
      }
    }
    return returnValue;
  }

  @Override
  public final void destroy(final Contextual<?> contextual) {
    // Nothing to do
  }

  @Override
  public final boolean isActive() {
    return true;
  }

  @Override
  public final Class<? extends Annotation> getScope() {
    return Dependent.class;
  }
  
}
