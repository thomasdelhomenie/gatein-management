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

package org.gatein.management.spi.plugin;

import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.binding.Marshaller;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface PluginRegistration extends ManagedResource.Registration
{

   /**
    * Get the {@link ManagedResource.Registration} object for a given address.
    * @param address the address of the subresource. <i>Note:</i> This can include path template variables.
    * @return the registration associated with the given address, null if none is found.
    */
   ManagedResource.Registration subResource(PathAddress address);

   /**
    * Registers a marshaller for a managed component.
    * 
    * <i>Note:</i> This will override a management extension's {@link org.gatein.management.api.binding.BindingProvider}
    * which supplies the marshaller if one has been registered for the same type and contentType.
    *
    * @param type the type of class
    * @param contentType the content type
    * @param marshaller the marshaller that will be responsible for marshalling and unmarshalling.
    */
   <T> void registerMarshaller(Class<T> type, ContentType contentType, Marshaller<T> marshaller);
}