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

package juzu.impl.router;

import juzu.impl.router.regex.RE;
import juzu.impl.router.regex.REFactory;
import juzu.test.AbstractTestCase;
import org.junit.Test;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PatternBuilderTestCase extends AbstractTestCase {

  @Test
  public void testEscapeReservedChar() throws Exception {
    assertLiteral('^');
    assertLiteral('*');
    assertLiteral('$');
    assertLiteral('[');
    assertLiteral(']');
    assertLiteral('.');
    assertLiteral('|');
    assertLiteral('+');
    assertLiteral('(');
    assertLiteral(')');
    assertLiteral('?');
  }

  private void assertLiteral(char c) {
    PatternBuilder pb = new PatternBuilder();
    pb.expr("^");
    pb.literal(c);
    pb.expr("$");
    RE pattern = REFactory.JAVA.compile(pb.build());
    assertTrue(pattern.matcher().matches(Character.toString(c)));
  }
}
