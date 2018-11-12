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

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

abstract class AbstractFunctionalContext implements AlterableContext {

  private final Consumer<? super Contextual<?>> destroyConsumer;
  
  private final Supplier<Class<? extends Annotation>> scopeSupplier;

  private final BooleanSupplier activeSupplier;
  
  AbstractFunctionalContext(final Consumer<? super Contextual<?>> destroyConsumer,
                            final Supplier<Class<? extends Annotation>> scopeSupplier,                            
                            final BooleanSupplier activeSupplier) {
    this.destroyConsumer = Objects.requireNonNull(destroyConsumer);
    this.scopeSupplier = Objects.requireNonNull(scopeSupplier);
    this.activeSupplier = Objects.requireNonNull(activeSupplier);
  }

  @Override
  public <T> T get(final Contextual<T> contextual) {
    return this.get(contextual, null);
  }

  @Override
  public void destroy(final Contextual<?> contextual) {
    this.destroyConsumer.accept(contextual);
  }
  
  @Override
  public Class<? extends Annotation> getScope() {
    return this.scopeSupplier.get();
  }

  @Override
  public boolean isActive() {
    return this.activeSupplier.getAsBoolean();
  }

}
