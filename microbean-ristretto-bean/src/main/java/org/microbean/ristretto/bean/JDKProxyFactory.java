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
package org.microbean.ristretto.bean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import java.util.Objects;
import java.util.Set;

import java.util.function.Supplier;

import javax.enterprise.inject.UnproxyableResolutionException;

import org.microbean.development.annotation.Hack;

import org.microbean.ristretto.type.Types;

@Hack("Just enough proxying to honor the CDI specification.")
public class JDKProxyFactory<T> implements ClientProxy.Factory<T> {

  @Override
  public T apply(final Supplier<T> contextualInstanceSupplier, final Set<? extends Type> beanTypes) {
    Objects.requireNonNull(contextualInstanceSupplier);
    Objects.requireNonNull(beanTypes);
    if (beanTypes.isEmpty()) {
      throw new IllegalArgumentException("beanTypes.isEmpty(): " + beanTypes);
    }
    try {
      Proxies.canProxy(contextualInstanceSupplier, beanTypes, false);
    } catch (final ReflectiveOperationException reflectiveOperationException) {
      throw new UnproxyableResolutionException(reflectiveOperationException.getMessage(), reflectiveOperationException);
    }
    // Reserve space for ClientProxy.class.
    final Class<?>[] classesArray = new Class<?>[beanTypes.size() + 1];
    Types.toClasses(beanTypes).toArray(classesArray);
    classesArray[classesArray.length - 1] = ClientProxy.class;
    try {
      @SuppressWarnings("unchecked")
      final T returnValue =
        (T)Proxy.newProxyInstance(contextualInstanceSupplier.getClass().getClassLoader(),
                                  classesArray,
                                  (proxy, method, args) -> invoke(contextualInstanceSupplier, proxy, method, args));
      return returnValue;
    } catch (IllegalArgumentException badTypes) {
      throw new UnproxyableResolutionException(badTypes.getMessage(), badTypes);
    }
  }

  private static final <T> Object invoke(final Supplier<T> contextualInstanceSupplier,
                                         final Object proxy,
                                         final Method method,
                                         final Object[] args)
    throws Throwable {
    Objects.requireNonNull(contextualInstanceSupplier);
    Objects.requireNonNull(method);
    // This will throw ContextNotActiveException per the specification
    // if the ContextualInstance is not active.  A ContextualInstance
    // is not active when its Context is not active.
    final T delegate = contextualInstanceSupplier.get();
    if (delegate == null) {
      throw new UnproxyableResolutionException(new IllegalStateException("contextualInstanceSupplier.get() == null: " + contextualInstanceSupplier));
    }
    return method.invoke(delegate, args);
  }
  
}
