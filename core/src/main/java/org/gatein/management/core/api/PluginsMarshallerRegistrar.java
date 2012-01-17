/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.management.core.api;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PluginsMarshallerRegistrar
{
   private final ConcurrentMap<String, Marshaller<?>> marshallers = new ConcurrentHashMap<String, Marshaller<?>>();

   PluginsMarshallerRegistrar()
   {
   }

   @SuppressWarnings("unchecked")
   public <T> Marshaller<T> getMarshaller(String componentName, Class<T> type, ContentType contentType) throws BindingException
   {
      return (Marshaller<T>) marshallers.get(key(componentName, type, contentType));
   }

   public <T> void registerMarshaller(String componentName, Class<T> type, ContentType contentType, Marshaller<T> marshaller)
   {
      marshallers.putIfAbsent(key(componentName, type, contentType), marshaller);
   }
   
   private static String key(String componentName, Class<?> type, ContentType contentType)
   {
      StringBuilder sb = new StringBuilder(componentName).append(":")
         .append(type.getName()).append(":").append(contentType.getName());
      
      return sb.toString();
   }
}
