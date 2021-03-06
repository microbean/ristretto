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

import java.util.function.Consumer;

import javax.enterprise.context.spi.CreationalContext;

class FunctionalCreationalContext<T> implements CreationalContext<T> {

  private final Consumer<T> pushConsumer;

  private final Runnable releaseRunnable;
  
  FunctionalCreationalContext(final Consumer<T> pushConsumer,
                              final Runnable releaseRunnable) {
    this.pushConsumer = Objects.requireNonNull(pushConsumer);
    this.releaseRunnable = Objects.requireNonNull(releaseRunnable);
  }

  @Override
  public void push(final T incompleteInstance) {
    this.pushConsumer.accept(incompleteInstance);
  }

  @Override
  public void release() {
    this.releaseRunnable.run();
  }
  
}
