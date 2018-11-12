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

import java.lang.reflect.Proxy;

import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import javax.enterprise.util.TypeLiteral;

import org.junit.Test;

import org.microbean.ristretto.context.Contexts;
import org.microbean.ristretto.context.ContextualInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestJDKProxyFactory {

  public TestJDKProxyFactory() {
    super();
  }

  @Test
  public void testBasicProxyBehavior() {
    final Integer fortyTwo = Integer.valueOf(42);
    final Contextual<Comparable<Integer>> contextual = new Contextual<>() {
        @Override
        public final Comparable<Integer> create(final CreationalContext<Comparable<Integer>> cc) {
          cc.push(fortyTwo);
          return fortyTwo;
        }
        
        @Override
        public final void destroy(final Comparable<Integer> instance,
                                  final CreationalContext<Comparable<Integer>> cc) {
          cc.release();
        }
      };
    final Contexts contexts = new Contexts();
    final CreationalContext<Comparable<Integer>> cc =
      contexts.createCreationalContext(contextual);
    final ContextualInstance<Comparable<Integer>> i =
      contexts.getContextualInstance(ApplicationScoped.class,
                                     contextual);
    assertSame(fortyTwo, i.get());
    final TypeLiteral<Comparable<Integer>> tl = new TypeLiteral<>() {};
    final ContextualReference<Comparable<Integer>> cr =
      new ContextualReference<Comparable<Integer>>(i,
                                                   tl.getType(),
                                                   new JDKProxyFactory<>());    
    final Comparable<Integer> ref = cr.get();
    assertTrue(Proxy.isProxyClass(ref.getClass()));
    assertEquals(0, ref.compareTo(fortyTwo));
  }
  
}
