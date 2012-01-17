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

package org.gatein.management.core;

import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.QueryOperationHandler;
import org.gatein.management.spi.ExtensionContext;
import org.gatein.management.spi.ManagementExtension;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestManagementExtension implements ManagementExtension
{
   @Override
   public void initialize(ExtensionContext context)
   {
      ComponentRegistration test = context.registerManagedComponent("test");
      test.registerBindingProvider(new TestBindingProvider());

      test.registerManagedResource(description("test managed component")).
         registerOperationHandler("test-operation", new TestOperationHandler(), description("Test operation"));
   }

   @Override
   public void destroy()
   {
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

   @SuppressWarnings("unchecked")
   private static class TestBindingProvider implements BindingProvider
   {
      @Override
      public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
      {
         if (type == Integer.class && contentType == ContentType.JSON)
         {
            return (Marshaller<T>) JsonIntegerMarshaller.INSTANCE;
         }

         return null;
      }
   }
   
   private static class JsonIntegerMarshaller implements Marshaller<Integer>
   {
      private static final JsonIntegerMarshaller INSTANCE = new JsonIntegerMarshaller();

      @Override
      public void marshal(Integer number, OutputStream outputStream) throws BindingException
      {
         try
         {
            String json = "{number: " + number + "}";
            outputStream.write(json.getBytes());
         }
         catch (IOException e)
         {
            throw new BindingException(e);
         }
      }

      @Override
      public Integer unmarshal(InputStream inputStream) throws BindingException
      {
         try
         {
            byte[] bytes = new byte[21];
            StringBuilder buffer = new StringBuilder();
            while (inputStream.read(bytes) != -1)
            {
               buffer.append(new String(bytes));
            }
            
            
            JSONObject json = new JSONObject(buffer.toString());
            return json.getInt("number");
         }
         catch (Exception e)
         {
            throw new BindingException(e);
         }
      }
   }

   public static final Integer TEST_INTEGER = 123;
   
   private static class TestOperationHandler extends QueryOperationHandler<Integer>
   {
      @Override
      protected Integer execute(OperationContext operationContext) throws ResourceNotFoundException, OperationException
      {
         return TEST_INTEGER;
      }
   }
}
