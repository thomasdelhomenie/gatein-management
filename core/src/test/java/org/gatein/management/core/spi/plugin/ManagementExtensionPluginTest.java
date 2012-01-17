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

package org.gatein.management.core.spi.plugin;

import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.exceptions.ManagementException;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.core.api.ManagementTest;
import org.gatein.management.core.api.SimpleManagedResource;
import org.gatein.management.spi.plugin.PluginContext;
import org.gatein.management.spi.plugin.PluginRegistration;
import org.junit.Test;

import static org.gatein.management.api.ManagedResource.*;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ManagementExtensionPluginTest extends ManagementTest
{
   @Test
   public void testRegistration()
   {
      SimpleManagedResource root = rootResource(SimpleManagedResource.class);
      root.registerSubResource("example", description("Example resource"));

      PluginContext pluginContext = pluginContext(root);
      assertNotNull(pluginContext.registerPlugin("example"));
   }

   @Test(expected = ManagementException.class)
   public void testRegistration_NoComponentExists()
   {
      SimpleManagedResource root = rootResource(SimpleManagedResource.class);
      root.registerSubResource("example", description("Example resource"));

      PluginContext pluginContext = pluginContext(root);
      pluginContext.registerPlugin("foo");
   }

   @Test
   public void testRegistration_WithPathTemplate()
   {
      SimpleManagedResource root = rootResource(SimpleManagedResource.class);
      Registration example = root.registerSubResource("example", description("Example resource"));
      example.registerSubResource("{template1: [a-zA-Z]*}", description("template1 resource"));
      example.registerSubResource("{template2: [0-9]{3}}", description("template2 resource"));

      PluginContext pluginContext = pluginContext(root);
      PluginRegistration plugin = pluginContext.registerPlugin("example");

      // Test plugin1 for template1 address space
      SimpleManagedResource plugin1 = (SimpleManagedResource) plugin.subResource(PathAddress.pathAddress("{template1}"));
      ManagedDescription desc1 = plugin1.getResourceDescription(PathAddress.empty());
      assertNotNull(desc1);
      assertEquals(desc1.getDescription(), "template1 resource");

      // Test plugin2 for template2 address space
      SimpleManagedResource plugin2 = (SimpleManagedResource) plugin.subResource(PathAddress.pathAddress("{template2}"));
      ManagedDescription desc2 = plugin2.getResourceDescription(PathAddress.empty());
      assertNotNull(desc2);
      assertEquals(desc2.getDescription(), "template2 resource");
   }
   
   @Test
   public void testNewOperationHandler()
   {
      SimpleManagedResource root = rootResource(SimpleManagedResource.class);
      root.registerSubResource("example", description("Example resource"));
      
      assertNull(root.getOperationHandler(PathAddress.pathAddress("example"), "foo"));

      PluginContext pluginContext = pluginContext(root);
      PluginRegistration plugin = pluginContext.registerPlugin("example");

      NoOpOperationHandler oh = new NoOpOperationHandler();
      plugin.registerOperationHandler("my-operation", oh, description("no op operation"));

      assertEquals(oh, root.getOperationHandler(PathAddress.pathAddress("example"), "my-operation"));
      assertNull(root.getOperationHandler(PathAddress.pathAddress("example"), "foo-operation"));
   }

   private static final class NoOpOperationHandler implements OperationHandler
   {
      @Override
      public void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
      {
      }
   }
}
