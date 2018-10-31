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

import javax.enterprise.context.Dependent;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

final class DependentContext extends AbstractContext {

  DependentContext() {
    super(Dependent.class, null /* no Storage */);
  }

  @Override
  public final <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
    final T returnValue = super.get(contextual, creationalContext);
    if (returnValue != null && creationalContext instanceof DependentInstanceCollection) {
      @SuppressWarnings("unchecked")
      final DependentInstanceCollection<T> dependentInstanceCollection = (DependentInstanceCollection<T>)creationalContext;
      dependentInstanceCollection.addDependentInstance(new ContextualInstance<>(contextual, returnValue, creationalContext));
    }
    return returnValue;
  }

}
