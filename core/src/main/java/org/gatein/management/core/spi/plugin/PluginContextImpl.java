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
import org.gatein.management.api.exceptions.ManagementException;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.core.api.PluginsMarshallerRegistrar;
import org.gatein.management.core.api.SimpleManagedResource;
import org.gatein.management.spi.plugin.PluginContext;
import org.gatein.management.spi.plugin.PluginRegistration;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PluginContextImpl implements PluginContext
{
   private final SimpleManagedResource root;
   private final PluginsMarshallerRegistrar registrar;

   public PluginContextImpl(SimpleManagedResource root, PluginsMarshallerRegistrar registrar)
   {
      this.root = root;
      this.registrar = registrar;
   }

   @Override
   public PluginRegistration registerPlugin(final String componentName)
   {
      final SimpleManagedResource componentResource = (SimpleManagedResource) root.getSubResource(componentName);

      if (componentResource == null)
         throw new ManagementException("Cannot register plugin for managed component " + componentName + " because it does not exist.");

      return new PluginRegistration()
      {
         @Override
         public ManagedResource.Registration subResource(PathAddress address)
         {
            SimpleManagedResource registration = componentResource;
            for (String name : address)
            {
               registration = (SimpleManagedResource) registration.getSubResource(name);
               if (registration == null) return null;
            }

            return registration;
         }

         @Override
         public <T> void registerMarshaller(Class<T> type, ContentType contentType, Marshaller<T> marshaller)
         {
            registrar.registerMarshaller(componentName, type, contentType, marshaller);
         }

         @Override
         public ManagedResource.Registration registerSubResource(String name, ManagedDescription description)
         {
            return componentResource.registerSubResource(name, description);
         }

         @Override
         public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description)
         {
            componentResource.registerOperationHandler(operationName, operationHandler, description);
         }

         @Override
         public void registerOperationHandler(String operationName, OperationHandler operationHandler, ManagedDescription description, boolean inherited)
         {
            componentResource.registerOperationHandler(operationName, operationHandler, description, inherited);
         }
      };
   }
}
