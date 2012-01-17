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

package org.gatein.management.example.plugin;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.ConversationState;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.AddOperationHandler;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.UpdateOperationHandler;
import org.gatein.management.spi.plugin.ManagementExtensionPlugin;
import org.gatein.management.spi.plugin.PluginContext;
import org.gatein.management.spi.plugin.PluginRegistration;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MopManagementPlugin implements ManagementExtensionPlugin
{
   @Override
   public void initialize(PluginContext context)
   {
      PluginRegistration registration = context.registerPlugin("mop");

      ManagedResource.Registration pages = registration.
         subResource(PathAddress.pathAddress("portalsites", "classic", "pages"));

      pages.registerOperationHandler(OperationNames.UPDATE_RESOURCE, new PageUpdateOperationHandler(), description("Updates a page."), true);
      pages.registerOperationHandler(OperationNames.ADD_RESOURCE, new PageAddOperationHandler(), description("Adds a page."));
   }

   @Override
   public void destroy()
   {
   }

   private static class PageUpdateOperationHandler extends UpdateOperationHandler<Page>
   {
      private static final Logger log = LoggerFactory.getLogger("org.gatein.management.mop.plugin");
      
      @Override
      protected void execute(OperationContext operationContext, Page page) throws ResourceNotFoundException, OperationException
      {
         log.info("Executing page update.");

         DataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);

         try
         {
            String siteType = operationContext.getAddress().resolvePathTemplate("site-type");
            String siteName = operationContext.getAddress().resolvePathTemplate("site-name");
            page.setOwnerId(siteName);
            page.setOwnerType(siteType);

            dataStorage.save(page);
            
            log.info("Successfully updated page " + page.getPageId());
         }
         catch (Exception e)
         {
            log.error("Exception updating page.", e);
            throw new OperationException(operationContext.getOperationName(), e.getMessage());
         }
      }
   }

   private static class PageAddOperationHandler extends AddOperationHandler<Page>
   {
      private static final Logger log = LoggerFactory.getLogger("org.gatein.management.mop.plugin");
      @Override
      protected Page execute(OperationContext operationContext)
      {
         String siteType = operationContext.getAddress().resolvePathTemplate("site-type");
         String siteName = operationContext.getAddress().resolvePathTemplate("site-name");
         
         String pageName = operationContext.getAttributes().getValue("pageName");
         if (pageName == null) throw new OperationException(operationContext.getOperationName(), "Attribute pageName required.");

         String title = operationContext.getAttributes().getValue("pageTitle");
         if (title == null) throw new OperationException(operationContext.getOperationName(), "Attribute pageTitle required.");

         ConversationState state = ConversationState.getCurrent();
         System.out.println("State: " + state);
         
         Page page = new Page(siteType, siteName, pageName);
         page.setTitle(title);
         page.setEditPermission(UserACL.EVERYONE);
         page.setAccessPermissions(new String [] {UserACL.EVERYONE});
         
         try
         {
            DataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);
            dataStorage.save(page);
         }
         catch (Exception e)
         {
            log.error("Error adding page " + page.getPageId(), e);
            throw new OperationException(operationContext.getOperationName(), "Could not add new page " + page.getId());
         }
         
         return page;
      }
   }
   
   private ManagedDescription description(final String description)
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
