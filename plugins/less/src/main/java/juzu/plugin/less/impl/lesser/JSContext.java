/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.plugin.less.impl.lesser;

import juzu.plugin.less.impl.lesser.jsr223.JSR223Context;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class JSContext {

  public static JSContext create() {
    JSContext context = JSR223Context.create();
    if (context == null) {
      // No JS available via JSR223
      try {
        Class<JSContext> type = (Class<JSContext>)JSContext.class.getClassLoader().loadClass("juzu.plugin.less.impl.lesser.rhino1_7R3.Rhino1_7R3Context");
        context = type.newInstance();
      }
      catch (Throwable e) {
        e.printStackTrace();
        // Cannot load it / should we log it ?
      }
    }
    if (context == null) {
      throw new UnsupportedOperationException("No JavaScript support available");
    }
    return context;
  }

  public abstract void put(String name, Object value);

  public abstract Object eval(String script) throws Exception;

  public abstract Object invokeFunction(String name, Object... args) throws Exception;

}
