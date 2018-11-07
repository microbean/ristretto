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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import javax.inject.Singleton;

public final class Contexts implements Destroyable {

  private final AtomicBoolean destroyed;
  
  private final Map<? extends Class<? extends Annotation>, ? extends Collection<? extends Context>> contexts;

  public Contexts() {
    this(defaultContexts());
  }
  
  public Contexts(final Map<? extends Class<? extends Annotation>, ? extends Collection<? extends Context>> contexts) {
    super();
    if (contexts == null) {
      this.contexts = Collections.emptyMap();
    } else {
      this.contexts = Collections.unmodifiableMap(new ConcurrentHashMap<>(contexts));
    }
    this.destroyed = new AtomicBoolean(false);
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
    this.contexts.forEach((k, v) -> {
        if (v instanceof Destroyable) {
          ((Destroyable)v).destroy();
        }
      });
    this.contexts.clear();
    return true;
  }

  public Context getContext(final Class<? extends Annotation> scope) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    Context returnValue = null;
    final Collection<? extends Context> contexts = this.contexts.get(scope);
    if (contexts != null && !contexts.isEmpty()) {
      for (final Context context : contexts) {
        if (context != null && context.isActive()) {
          returnValue = context;
          break;
        }
      }
    }
    if (returnValue == null || !returnValue.isActive()) {
      // We check the activeness one last time here because it's the
      // best effort we can make in a multithreaded scenario.  See
      // http://lists.jboss.org/pipermail/weld-dev/2018-November/003716.html
      // and
      // http://lists.jboss.org/pipermail/weld-dev/2018-November/003717.html.
      throw new ContextNotActiveException(scope.toString());
    }
    return returnValue;
  }

  public final <T> CreationalContext<T> createCreationalContext(final Contextual<T> contextual) {
    return new org.microbean.ristretto.context.CreationalContext<>(contextual);
  }
  
  public final <T> ContextualInstance<T> getContextualInstance(final Class<? extends Annotation> scope,
                                                               final Contextual<T> contextual,
                                                               final CreationalContext<T> creationalContext) {
    if (this.isDestroyed()) {
      throw new IllegalStateException();
    }
    final Context context = this.getContext(scope);
    if (context == null) {
      throw new ContextNotActiveException();
    }
    return new ContextualInstance<>(context.get(contextual, creationalContext), contextual::destroy, creationalContext, context::isActive, isNormalScope(scope));
  }

  public static final boolean isNormal(final Context context) {
    return context != null && isNormalScope(context.getScope());
  }
  
  public static final boolean isNormalScope(final Annotation scopeTypeAnnotation) {
    Objects.requireNonNull(scopeTypeAnnotation);
    return isNormalScope(Set.of(scopeTypeAnnotation.annotationType().getAnnotations()), null);
  }
  
  public static final boolean isNormalScope(final Class<? extends Annotation> scopeTypeAnnotation) {
    Objects.requireNonNull(scopeTypeAnnotation);
    return isNormalScope(Set.of(scopeTypeAnnotation.getAnnotations()), null);
  }

  public static final boolean isNormalScope(final Supplier<? extends Set<? extends Annotation>> annotationSupplier) {
    return isNormalScope(annotationSupplier.get(), null);
  }

  public static final boolean isNormalScope(final Set<? extends Annotation> annotations) {
    return isNormalScope(annotations, null);
  }

  private static final boolean isNormalScope(final Set<? extends Annotation> annotations, Set<? super Annotation> seen) {
    boolean returnValue = false;
    if (annotations != null && !annotations.isEmpty()) {
      if (seen == null) {
        seen = new HashSet<>();
      }
      for (final Annotation annotation : annotations) {
        if (annotation != null && !seen.contains(annotation)) {
          seen.add(annotation);
          final Class<? extends Annotation> annotationType = annotation.annotationType();
          if (NormalScope.class.isAssignableFrom(annotationType) ||
              isNormalScope(annotationType, seen)) {
            returnValue = true;
            break;
          }
        }
      }
    }
    return returnValue;
  }

  private static final boolean isNormalScope(final Class<? extends Annotation> scopeTypeAnnotation, final Set<? super Annotation> seen) {
    Objects.requireNonNull(scopeTypeAnnotation);
    return isNormalScope(Set.of(scopeTypeAnnotation.getAnnotations()), seen);
  }
  
  private static final Map<? extends Class<? extends Annotation>, ? extends Collection<? extends Context>> defaultContexts() {
    final Map<Class<? extends Annotation>, Collection<? extends Context>> returnValue = new HashMap<>();
    returnValue.put(ApplicationScoped.class, Collections.singleton(new ApplicationContext()));
    returnValue.put(Dependent.class, Collections.singleton(new DependentContext()));
    returnValue.put(Singleton.class, Collections.singleton(new SingletonContext()));
    return returnValue;
  }
  
}
