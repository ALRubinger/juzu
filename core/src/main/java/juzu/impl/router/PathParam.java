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

import juzu.impl.router.regex.RENode;
import juzu.impl.router.regex.REParser;
import juzu.impl.router.regex.RERenderer;
import juzu.impl.router.regex.REVisitor;
import juzu.impl.router.regex.SyntaxException;
import juzu.impl.common.QualifiedName;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PathParam {

  /** . */
  final QualifiedName name;

  /** . */
  final boolean preservePath;

  /** . */
  final String routingRegex;

  /** . */
  final RERef[] matchingRegex;

  /** . */
  final String[] templatePrefixes;

  /** . */
  final String[] templateSuffixes;

  private PathParam(
      QualifiedName name,
      boolean preservePath,
      String routingRegex,
      RERef[] matchingRegex,
      String[] templatePrefixes,
      String[] templateSuffixes) {

    //
    if (name == null) {
      throw new NullPointerException("No null name accepted");
    }
    if (matchingRegex == null || matchingRegex.length == 0) {
      throw new NullPointerException("No null or empty pattern accepted");
    }

    //
    this.name = name;
    this.preservePath = preservePath;
    this.routingRegex = routingRegex;
    this.matchingRegex = matchingRegex;
    this.templatePrefixes = templatePrefixes;
    this.templateSuffixes = templateSuffixes;
  }

  public QualifiedName getName() {
    return name;
  }

  @Override
  public String toString() {
    return "PathParam[name=" + name + ",preservePath=" + preservePath + ",pattern=" + matchingRegex + "]";
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    /** . */
    private String pattern;

    /** . */
    private boolean preservePath;

    /** . */
    private boolean captureGroup;

    private Builder() {
      this.preservePath = false;
      this.captureGroup = false;
    }

    PathParam build(Router router, QualifiedName name) {

      Builder descriptor = this;

      //
      if (descriptor == null) {
        throw new NullPointerException("No null descriptor accepted");
      }

      //
      String regex;
      boolean preservePath = false;
      if (descriptor != null) {
        regex = descriptor.getPattern();
        preservePath = descriptor.preservePath;
      } else {
        regex = null;
      }

      //
      if (regex == null) {
        regex = preservePath ? "[^/]+" : ".+";
      }

      // Now work on the regex
      StringBuilder routingRegex = new StringBuilder();
      RERef[] renderingRegexes;
      String[] templatePrefixes;
      String[] templateSuffixes;
      try {
        REVisitor<RuntimeException> transformer = descriptor.getCaptureGroup() ?
            new CaptureGroupTransformation() : new NonCaptureGroupTransformation();
        REParser parser = new REParser(regex);

        //
        RENode.Disjunction routingDisjunction = parser.parseDisjunction();
        if (!preservePath) {
          CharEscapeTransformation escaper = new CharEscapeTransformation('/', '_');
          routingDisjunction.accept(escaper);
        }
        routingDisjunction.accept(transformer);
        RERenderer.render(routingDisjunction, routingRegex);

        //
        parser.reset();
        RENode.Disjunction renderingDisjunction = parser.parseDisjunction();
        ValueResolverFactory factory = new ValueResolverFactory();
        renderingDisjunction.accept(transformer);
        List<ValueResolverFactory.Alternative> alt = factory.foo(renderingDisjunction);
        renderingRegexes = new RERef[alt.size()];
        templatePrefixes = new String[alt.size()];
        templateSuffixes = new String[alt.size()];
        for (int i = 0;i < alt.size();i++) {
          ValueResolverFactory.Alternative v = alt.get(i);
          StringBuilder valueMatcher = v.getValueMatcher();
          valueMatcher.insert(0, '^');
          valueMatcher.append("$");
          renderingRegexes[i] = router.compile(valueMatcher.toString());
          templatePrefixes[i] = v.getPrefix();
          templateSuffixes[i] = v.getSuffix();
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      catch (SyntaxException e) {
        throw new RuntimeException(e);
      }
      catch (MalformedRouteException e) {
        throw new RuntimeException(e);
      }

      //
      return new PathParam(
          name,
          preservePath,
          routingRegex.toString(),
          renderingRegexes,
          templatePrefixes,
          templateSuffixes);
    }

    public Builder matchedBy(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder captureGroup(boolean capture) {
      this.captureGroup = capture;
      return this;
    }

    public Builder preservePath(boolean preservePath) {
      this.preservePath = preservePath;
      return this;
    }

    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }

    public boolean getCaptureGroup() {
      return captureGroup;
    }

    public void setCaptureGroup(boolean captureGroup) {
      this.captureGroup = captureGroup;
    }
  }
}
