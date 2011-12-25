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

package org.gatein.management.spi;

/**
 * The main interface for providing a management extension. The extension is looked up via the {@link java.util.ServiceLoader}
 * class, so extensions must provide the META-INF/services convention for loading this interface.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public interface ManagementExtension
{
   /**
    * Called when the extension is loaded by the {@link java.util.ServiceLoader} class.
    *
    * @param context the extension context for registering a managed component.
    */
   void initialize(ExtensionContext context);

   /**
    * This method is to provide extensions the opportunity to release any resources it may have. This method is called
    * when the {@link org.gatein.management.api.ManagementService#reloadExtensions()} or {@link org.gatein.management.api.ManagementService#unload()}
    * is invoked.
    *
    * <i>Note:</i> This is not called when artifacts are undeployed in the context of an application server.
    */
   void destroy();
}
