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

import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.ManagementException;
import org.gatein.management.spi.plugin.ManagementExtensionPlugin;
import org.gatein.management.spi.plugin.PluginContext;
import org.gatein.management.spi.plugin.PluginRegistration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestManagementExtensionPlugin implements ManagementExtensionPlugin
{
   @Override
   public void initialize(PluginContext context) throws ManagementException
   {
      PluginRegistration registration = context.registerPlugin("test");
      registration.registerMarshaller(Integer.class, ContentType.XML, new Marshaller<Integer>()
      {
         @Override
         public void marshal(Integer number, OutputStream outputStream) throws BindingException
         {
            String xml = "<number>" + number + "</number>";
            try
            {
               outputStream.write(xml.getBytes());
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

               String xml = buffer.toString();
               int begin = xml.indexOf('>');
               int end = xml.indexOf('<', begin);
               String number = xml.substring(begin+1, end);
               return new Integer(number);
            }
            catch (Exception e)
            {
               throw new BindingException(e);
            }
         }
      });

      registration.registerMarshaller(Integer.class, ContentType.JSON, new Marshaller<Integer>()
      {
         @Override
         public void marshal(Integer object, OutputStream outputStream) throws BindingException
         {
            try
            {
               outputStream.write("plugin overwrite".getBytes());
            }
            catch (IOException e)
            {
               throw new BindingException(e);
            }
         }

         @Override
         public Integer unmarshal(InputStream inputStream) throws BindingException
         {
            return -21;
         }
      });
   }

   @Override
   public void destroy() throws ManagementException
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
