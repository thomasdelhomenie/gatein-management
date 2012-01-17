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
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.core.TestManagementExtension;
import org.gatein.management.core.api.controller.SimpleManagementController;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ManagementServiceTest
{
   @Test
   public void testExtension_NoPlugins() throws Exception
   {
      ManagementServiceImpl service = new ManagementServiceImpl();
      service.initializeExtensions(createRootResource());

      ManagedResource root = service.getManagedResource(PathAddress.empty());
      assertNotNull(root);

      ManagedResource test = root.getSubResource(PathAddress.pathAddress("test"));
      assertNotNull(test);
      assertNotNull(root.getOperationHandler(PathAddress.pathAddress("test"), "test-operation"));

      ManagementController controller = new SimpleManagementController(service, null);

      ManagedResponse response =
         controller.execute(ManagedRequest.Factory.create("test-operation", PathAddress.pathAddress("test"), ContentType.JSON));

      assertTrue(response.getOutcome().isSuccess());
      assertTrue(response.getResult() instanceof Integer);
      assertEquals(TestManagementExtension.TEST_INTEGER, response.getResult());
      ByteArrayOutputStream baos = new ByteArrayOutputStream(21);
      response.writeResult(baos);
      assertEquals("{number: " + TestManagementExtension.TEST_INTEGER + "}", new String(baos.toByteArray()));
   }

   @Test
   public void testExtension_WithPlugins() throws Exception
   {
      ManagementServiceImpl service = new ManagementServiceImpl();
      service.load();

      ManagedResource root = service.getManagedResource(PathAddress.empty());
      assertNotNull(root);

      ManagedResource test = root.getSubResource(PathAddress.pathAddress("test"));
      assertNotNull(test);
      assertNotNull(root.getOperationHandler(PathAddress.pathAddress("test"), "test-operation"));

      ManagementController controller = new SimpleManagementController(service, null);

      // Test plugin added new marshaller for XML
      ManagedResponse response =
         controller.execute(ManagedRequest.Factory.create("test-operation", PathAddress.pathAddress("test"), ContentType.XML));

      assertTrue(response.getOutcome().isSuccess());
      assertTrue(response.getResult() instanceof Integer);
      assertEquals(TestManagementExtension.TEST_INTEGER, response.getResult());
      ByteArrayOutputStream baos = new ByteArrayOutputStream(21);
      response.writeResult(baos);
      assertEquals("<number>" + TestManagementExtension.TEST_INTEGER + "</number>", new String(baos.toByteArray()));

      // Test plugin changed marshaller for JSON
      response = controller.execute(
         ManagedRequest.Factory.create("test-operation", PathAddress.pathAddress("test"), ContentType.JSON));

      assertTrue(response.getOutcome().isSuccess());
      assertTrue(response.getResult() instanceof Integer);
      assertEquals(123, response.getResult());
      baos = new ByteArrayOutputStream(16);
      response.writeResult(baos);
      assertEquals("plugin overwrite", new String(baos.toByteArray()));
   }

   private SimpleManagedResource createRootResource()
   {
      return new SimpleManagedResource(null, null, description("Root resource"));
   }

   private static ManagedDescription description(final String description)
   {
      return new ManagedDescription()
      {
         @Override
         public String getDescription()
         {
            return description;
         }
      };
   }
}
