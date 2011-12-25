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

package org.gatein.management.core.spi.plugin;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.core.api.SimpleManagedResource;
import org.gatein.management.spi.plugin.PluginContext;
import org.gatein.management.spi.plugin.PluginRegistration;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PluginContextImpl implements PluginContext
{
   private final SimpleManagedResource resource;

   public PluginContextImpl(SimpleManagedResource resource)
   {
      this.resource = resource;
   }

   @Override
   public PluginRegistration registerPlugin(String managedComponentName, int priority)
   {
      return new PluginRegistration()
      {
         @Override
         public ManagedResource.Registration getRegistration(PathAddress address)
         {
            return (ManagedResource.Registration) resource.getSubResource(address);
         }

         @Override
         public <T> void registerMarshaller(Class<T> type, ContentType contentType, Marshaller<T> marshaller)
         {
            //TODO: Implement me !
            throw new UnsupportedOperationException();
         }

         @Override
         public ManagedResource.Registration registerSubResource(String name, ManagedDescription description)
         {
            return resource.registerSubResource(name, description);
         }

         @Override
         public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description)
         {
            resource.registerOperationHandler(operationName, operationHandler, description);
         }

         @Override
         public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description, boolean inherited)
         {
            resource.registerOperationHandler(operationName, operationHandler, description, inherited);
         }
      };
   }
}
