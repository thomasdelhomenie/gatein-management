/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.management.portalobjects.binding.impl.site;

import org.gatein.staxbuilder.EnumAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Attribute implements EnumAttribute<Attribute>
{
   UNKNOWN(null),
   PROPERTIES_KEY("key");

   private final String name;

   Attribute(final String name)
   {
      this.name = name;
   }

   /**
    * Get the local name of this element.
    *
    * @return the local name
    */
   public String getLocalName()
   {
      return name;
   }

   private static final Map<String, Attribute> MAP;

   static
   {
      final Map<String, Attribute> map = new HashMap<String, Attribute>();
      for (Attribute attribute : values())
      {
         final String name = attribute.getLocalName();
         if (name != null) map.put(name, attribute);
      }
      MAP = map;
   }

   public static Attribute forName(String localName)
   {
      final Attribute attribute = MAP.get(localName);
      return attribute == null ? UNKNOWN : attribute;
   }
}
