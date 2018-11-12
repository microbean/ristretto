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

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Supplier;

public abstract class DestroyableSupplier<T> implements Destroyable, Supplier<T> {

  private final AtomicBoolean destroyed;

  protected DestroyableSupplier() {
    this.destroyed = new AtomicBoolean();
  }

  @Override
  public final boolean isDestroyed() {
    return this.destroyed.get();
  }
      
  @Override
  public final boolean destroy() {
    if (this.destroyed.getAndSet(true)) {
      throw new IllegalStateException();
    }
    return this.performDestruction();
  }

  protected abstract boolean performDestruction();

}
