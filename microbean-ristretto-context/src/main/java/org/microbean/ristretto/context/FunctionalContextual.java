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

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

class FunctionalContextual<T> implements Contextual<T> {

  private final Function<CreationalContext<T>, T> creator;

  private final BiConsumer<T, CreationalContext<T>> destroyer;

  FunctionalContextual(final Function<CreationalContext<T>, T> creator) {
    this(creator, null);
  }

  FunctionalContextual(final BiConsumer<T, CreationalContext<T>> destroyer) {
    this(null, destroyer);
  }
  
  FunctionalContextual(final Function<CreationalContext<T>, T> creator,
                       final BiConsumer<T, CreationalContext<T>> destroyer) {
    super();
    this.creator = creator;
    this.destroyer = destroyer;
  }

  @Override
  public T create(final CreationalContext<T> creationalContext) {
    if (this.creator == null) {
      throw new IllegalStateException();
    } else {
      return this.creator.apply(creationalContext);
    }
  }

  @Override
  public void destroy(final T instance, final CreationalContext<T> creationalContext) {
    if (this.destroyer == null) {
      creationalContext.release();
    } else {
      // The destroyer function is obligated to call release()
      this.destroyer.accept(instance, creationalContext);
    }
  }
  
}
