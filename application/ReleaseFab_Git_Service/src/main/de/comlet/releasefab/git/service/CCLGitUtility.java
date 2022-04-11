/**
 * ReleaseFab
 *
 * Copyright © 2022 comlet Verteilte Systeme GmbH
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * @file CCLGitUtility.java
 *
 * @brief Git utility.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.api.almservice.ICLALMUtility;
import de.comlet.releasefab.api.plugin.ACLDeliveryInformation;
import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.api.vcsservice.ICLTagContainer;
import de.comlet.releasefab.api.vcsservice.ICLVersionControlUtility;
import de.comlet.releasefab.git.classes.CCLGitTagContainer;
import de.comlet.releasefab.git.classes.CCLXMLGitConstants;
import de.comlet.releasefab.library.exception.CCLVersionControlException;
import de.comlet.releasefab.library.model.CCLComponent;
import de.comlet.releasefab.library.model.CCLDelivery;
import de.comlet.releasefab.library.model.CCLTuple;
import de.comlet.releasefab.library.xml.CCLXMLSinkBase;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Element;

/**
 * Provide an empty Iterator.
 */
class CCLNullIterable<T> implements Iterable<T>
{
   class CCLNullIterator<K> implements Iterator<K>
   {
      /**
       * By definition there can never be a next element.
       */
      @Override
      public boolean hasNext()
      {
         return false;
      }

      /**
       * Always return null.
       */
      @Override
      public K next()
      {
         return null;
      }

      /**
       * Unsupported operation.
       */
      @Override
      public void remove()
      {
         throw new UnsupportedOperationException();
      }
   }

   /**
    * Provide an empty iterator object.
    */
   @Override
   public Iterator<T> iterator()
   {
      return new CCLNullIterator<>();
   }
}

/**
 * Encapsulates common code concerning Git.
 */
public class CCLGitUtility implements ICLVersionControlUtility
{
   protected CCLGitHandler mGit;

   /**
    * Constructor. Create Git handler.
    * 
    * @param path to a Git repository
    */
   public CCLGitUtility(String path) throws CCLVersionControlException
   {
      localInit(path);
   }

   /**
    * Constructor. Creation without Git handler! The method
    * {@link #initializeHandler(String)} has to be called afterwards.
    */
   public CCLGitUtility()
   {
   }

   /**
    * Initialize this class locally. Added due to a Sonar warning.
    * 
    * @param path
    * @throws CCLVersionControlException
    */
   private void localInit(String path) throws CCLVersionControlException
   {
      initializeHandler(path);
   }

   /**
    * Initializes a Git handler using a Git configuration object.
    */
   @Override
   public void initializeHandler(String path) throws CCLVersionControlException
   {
      CCLGitConfig config = new CCLGitConfig(path);
      mGit = new CCLGitHandler(config);
   }

   /**
    * Get an Iterator for commits compliant with the defined rules.
    * 
    * @param formerTag
    * @return iterator
    */
   @Override
   public Iterable<ICLCommitContainer> getIterator(ICLTagContainer formerTag) throws CCLVersionControlException
   {
      CCLGitTagContainer latestTag = mGit.isHeadTag();
      if (latestTag == null)
      {
         throw new CCLVersionControlException("Head is not synched to a tag. Documentation stopped.");
      }

      Iterable<ICLCommitContainer> commits;
      if (formerTag == null)
      {
         // all commits; starting from current head
         commits = mGit.getAllCommitsIterator();
      }
      else
      {
         // if there is no new tag, provide a null iterator
         if (latestTag.getHash().equals(((CCLGitTagContainer) formerTag).getHash()))
         {
            commits = new CCLNullIterable<>();
         }
         else
         {
            commits = mGit.getRangeIterator(latestTag.getPointsToHash(),
                  ((CCLGitTagContainer) formerTag).getPointsToHash());
         }
      }

      return commits;
   }

   /**
    * Closes Git handler.
    * 
    * @throws CCLVersionControlException
    */
   @Override
   public void close()
   {
      if (mGit != null)
      {
         mGit.close();
         mGit = null;
      }
   }

   /**
    * Checks, if head revision is a TAG.
    * 
    * @throws CCLVersionControlException
    */
   @Override
   public CCLGitTagContainer isSyncedToTag() throws CCLVersionControlException
   {
      return mGit.isHeadTag();
   }

   /**
    * Get name of the current branch.
    * 
    * @throws CCLVersionControlException
    */
   @Override
   public String getCurrentBranch() throws CCLVersionControlException
   {
      return mGit.getCurrentBranch();
   }

   /**
    * Get Iterable of Git commit containers filtered by the status of their
    * corresponding ALM Item.
    * 
    * @throws CCLVersionControlException
    */
   @Override
   public Iterable<ICLCommitContainer> getCommitFilter(Iterable<ICLCommitContainer> source, ICLALMUtility almUtil)
         throws CCLVersionControlException
   {
      if (null == almUtil)
      {
         throw new CCLVersionControlException(this.getClass().getName() + " - getCommitFilter: Handler is null");
      }
      return new CCLCommitFilter(source, almUtil);
   }

   /**
    * Get former documented TAG.
    * 
    * @return TAG or null if no TAG has been found.
    */
   @Override
   public ICLTagContainer getFormerTag(CCLComponent component, CCLDelivery currentDelivery, CCLDelivery formerDelivery)
   {
      ICLTagContainer retValue = null;

      if (formerDelivery != null)
      {
         String key = formerDelivery.getName() + CCLXMLGitConstants.IMPORTER_NAME;
         ACLDeliveryInformation deliveryInfo = component.getDeliveryInformation(key);

         if (deliveryInfo != null)
         {
            Element element = deliveryInfo.getInformation();
            retValue = readTags(element).getSecond();
         }
      }
      return retValue;
   }

   /**
    * Create string with information about a branch for the UI.
    */
   @Override
   public String getBranchInformation(Element information)
   {
      String branchName = "";
      List<Element> branchList = information.getChildren(CCLXMLGitConstants.XML_GIT_BRANCH);
      if (!branchList.isEmpty())
      {
         branchName = branchList.get(0).getText();
      }
      return "Branch: " + branchName;
   }

   /**
    * Create String with information about a TAG for the UI.
    */
   @Override
   public String getTagInformation(Element information)
   {
      CCLTuple<ICLTagContainer, ICLTagContainer> tags = readTags(information);

      // if there is no former tag, provide default text
      String tagOld = "[initial]";
      if (tags.getFirst() != null)
      {
         tagOld = tags.getFirst().getName();
      }

      String tagNew = "";
      if (tags.getSecond() != null)
      {
         tagNew = tags.getSecond().getName();
      }

      return "Tags: " + tagOld + " - " + tagNew;
   }

   /**
    * Read former and latest TAG from XML-Source.
    * 
    * @return tuple of tags: first -> former; second -> latest. Tuple contains
    * null elements, if there are no tags
    */
   private CCLTuple<ICLTagContainer, ICLTagContainer> readTags(Element source)
   {
      CCLTuple<ICLTagContainer, ICLTagContainer> result = new CCLTuple<>(null, null);

      List<Element> tagList = source.getChildren(CCLXMLGitConstants.XML_GIT_TAG);
      for (Element tag : tagList)
      {
         String tagType = tag.getAttributeValue(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE);
         if (null != tagType)
         {
            if (tagType.compareTo(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE_FORMER) == 0)
            {
               result.setFirst(toContainer(tag));
            }
            else if (tagType.compareTo(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TYPE_LATEST) == 0)
            {
               result.setSecond(toContainer(tag));
            }
         }
      }
      return result;
   }

   /**
    * Do transformation from XML-Element to CCLGitTagContainer.
    */
   private CCLGitTagContainer toContainer(Element tag)
   {
      return new CCLGitTagContainer(tag.getText(), new Date(0),
            tag.getAttributeValue(CCLXMLGitConstants.XML_GIT_TAG_ATTR_HASH),
            tag.getAttributeValue(CCLXMLGitConstants.XML_GIT_TAG_ATTR_TARGET));
   }

   /**
    * Constructs a CCLXMLSinkBase with basic information about the current revision.
    */
   @Override
   public CCLXMLSinkBase getVersionControlXMLSink(ICLTagContainer formerTag, ICLTagContainer currentTag)
         throws CCLVersionControlException
   {
      CCLXMLGitSink sink = new CCLXMLGitSink();
      sink.addBranch(getCurrentBranch());
      sink.addTags(formerTag, currentTag);
      return sink;
   }
}
