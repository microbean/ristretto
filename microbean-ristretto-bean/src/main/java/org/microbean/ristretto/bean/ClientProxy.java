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

import java.lang.reflect.Type;

import java.util.Set;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@FunctionalInterface
public interface ClientProxy<T> {

  T $get();

  @FunctionalInterface
  public static interface Factory<T> extends BiFunction<Supplier<T>, Set<? extends Type>, T> {
    // Section 5.4: "A client proxy implements/extends some or all of
    // the bean types of the bean"
    //
    // 

  }
  
}
