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

package org.gatein.management.core.api;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.ManagementService;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.core.api.binding.GlobalBindingProvider;
import org.gatein.management.core.api.operation.global.GlobalOperationHandlers;
import org.gatein.management.core.spi.ExtensionContextImpl;
import org.gatein.management.core.spi.plugin.PluginContextImpl;
import org.gatein.management.spi.ExtensionContext;
import org.gatein.management.spi.ManagementExtension;
import org.gatein.management.spi.plugin.ManagementExtensionPlugin;
import org.gatein.management.spi.plugin.PluginContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ManagementServiceImpl implements ManagementService
{
   private static final Logger log = LoggerFactory.getLogger(ManagementService.class);

   private ManagedResource rootResource;
   private List<ManagementExtension> extensions;
   private Map<String, BindingProvider> bindingProviders;
   private PluginsMarshallerRegistrar pluginsBindingProvider;
   private BindingProvider globalBindingProvider;
   private List<ManagementExtensionPlugin> plugins;

   @Override
   public ManagedResource getManagedResource(PathAddress address)
   {
      if (rootResource == null) return null;

      return rootResource.getSubResource(address);
   }

   @Override
   public BindingProvider getBindingProvider(final String componentName)
   {
      return new BindingProvider()
      {
         @Override
         public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
         {
            Marshaller<T> marshaller = null;

            // Check plugin marshallers
            if (pluginsBindingProvider != null) // This is here for tests to load extensions w/out plugins
            {
               marshaller = pluginsBindingProvider.getMarshaller(componentName, type, contentType);
               if (marshaller != null) return marshaller;
            }

            // Check extension marshallers
            BindingProvider bp = bindingProviders.get(componentName);
            if (bp != null)
            {
               marshaller = bp.getMarshaller(type, contentType);
            }
            if (marshaller != null) return marshaller;

            // Return any global marshallers
            return globalBindingProvider.getMarshaller(type, contentType);
         }
      };
   }

   @Override
   public void reloadExtensions()
   {
      // Make sure this operation is atomic
      synchronized (ManagementServiceImpl.class)
      {
         unload();
         load();
      }
   }

   @Override
   public synchronized void load()
   {
      log.info("Management service module loading");

      // Create root managed resource
      SimpleManagedResource resource = new SimpleManagedResource(null, null, new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return "Root management resource.";
         }
      });

      // load extensions
      initializeExtensions(resource);

      // load plugins
      initializePlugins(resource);

      // register global operations
      initGlobalOperations(resource);
      globalBindingProvider = new GlobalBindingProvider();
   }

   @Override
   public synchronized void unload()
   {
      log.info("Management service module unloading");

      boolean debug = log.isDebugEnabled();
      if (plugins != null)
      {
         for (ManagementExtensionPlugin plugin : plugins)
         {
            if (debug) log.debug("Calling destroy() on management extension plugin " + plugin);
            plugin.destroy();
         }
         plugins.clear();
      }
      
      if (extensions != null)
      {
         for (ManagementExtension extension : extensions)
         {
            if (debug) log.debug("Calling destroy() on management extension " + extension.getClass());
            extension.destroy();
         }
         extensions.clear();
      }

      rootResource = null;
   }

   void initializeExtensions(SimpleManagedResource resource)
   {
      List<ManagementExtension> list = new ArrayList<ManagementExtension>();
      Map<String, BindingProvider> map = new HashMap<String, BindingProvider>();
      ExtensionContext context = new ExtensionContextImpl(resource, map);

      ServiceLoader<ManagementExtension> extensionServiceLoader = ServiceLoader.load(ManagementExtension.class);
      for (ManagementExtension extension : extensionServiceLoader)
      {
         extension.initialize(context);
         list.add(extension);
      }
      
      extensions = list;
      bindingProviders = map;
      rootResource = resource;
      log.debug("Successfully loaded " + extensions.size() + " management extension(s).");
   }

   void initializePlugins(SimpleManagedResource resource)
   {
      List<ManagementExtensionPlugin> list = new ArrayList<ManagementExtensionPlugin>();

      PluginsMarshallerRegistrar registrar = new PluginsMarshallerRegistrar();
      PluginContext pluginContext = new PluginContextImpl(resource, registrar);
      ServiceLoader<ManagementExtensionPlugin> pluginServiceLoader = ServiceLoader.load(ManagementExtensionPlugin.class);
      for (ManagementExtensionPlugin plugin : pluginServiceLoader)
      {
         log.debug("Initializing management extension plugin " + plugin);
         plugin.initialize(pluginContext);
         list.add(plugin);
      }
      
      plugins = list;
      pluginsBindingProvider = registrar;

      log.debug("Successfully loaded " + plugins.size() + " management extension plugin(s).");
   }

   private void initGlobalOperations(ManagedResource.Registration registration)
   {
      registration.registerOperationHandler(OperationNames.READ_RESOURCE, GlobalOperationHandlers.READ_RESOURCE, GlobalOperationHandlers.READ_RESOURCE, true);
   }
}
