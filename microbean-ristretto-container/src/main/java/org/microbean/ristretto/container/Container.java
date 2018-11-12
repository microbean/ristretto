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
package org.microbean.ristretto.container;

import java.lang.annotation.Annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.spi.Context;

class Container {

  private final Map<Class<? extends Annotation>, Context> contexts;
  
  Container() {
    super();
    this.contexts = new HashMap<>();
  }

  void addContext(final Context context) {
    this.contexts.putIfAbsent(context.getScope(), context);
  }

  Context getContext(final Class<? extends Annotation> scope) {
    return this.contexts.get(scope);
  }

  
  
}
