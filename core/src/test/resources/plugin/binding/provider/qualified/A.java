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

package plugin.binding.provider.qualified;

import juzu.Response;
import juzu.View;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A {

  @Inject
  @Named("foo")
  Service fooService;

  @Inject
  @Named("bar")
  Service barService;

  @View
  public Response.Content index() throws IOException {
    String resp;
    if (fooService == null) {
      resp = "failed: no foo service";
    }
    else {
      String name = fooService.getName();
      if ("foo".equals(name)) {
        if (barService == null) {
          resp = "failed: no bar service";
        }
        else {
          name = barService.getName();
          if ("bar".equals(name)) {
            resp = "pass";
          }
          else {
            resp = "failed: wrong bar name " + name;
          }
        }
      }
      else {
        resp = "failed: wrong foo name " + name;
      }
    }
    return Response.ok(resp);
  }
}
