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
 * @file CCLGitHandler.java
 *
 * @brief Git handler.
 */

package de.comlet.releasefab.git.service;

import de.comlet.releasefab.api.vcsservice.ICLCommitContainer;
import de.comlet.releasefab.git.classes.CCLGitCommitContainer;
import de.comlet.releasefab.git.classes.CCLGitTagContainer;
import de.comlet.releasefab.library.exception.CCLVersionControlException;
import de.comlet.releasefab.library.exception.CCLVersionControlRuntimeException;
import de.comlet.releasefab.library.settings.SCLSettings;
import de.comlet.releasefab.library.settings.SCLSettings.ECLSettingsType;
import de.comlet.releasefab.library.xml.CCLXMLConstants;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

/**
 * Encapsulate Git API calls.
 */
public final class CCLGitHandler implements AutoCloseable
{
   private static final String COMMIT_TEMPLATE = SCLSettings.get(CCLXMLConstants.XML_COMMIT_TEMPLATE, EnumSet.of(ECLSettingsType.PROJECT));
   protected static final String GIT_HEAD = "HEAD";
   private static final String IO_ERROR_EXCEPTION = "IO Error! ";
   private static final int HASH_LEN = 8;

   protected String mPathToRepo;
   protected Git mGit;

   /**
    * Iterate over Item IDs from Git commit messages. Transform RevCommit to
    * CCLCommitContainer.
    */
   protected class CCLCommitIterable implements Iterable<ICLCommitContainer>
   {
      private ObjectId mFromObject;
      private ObjectId mToObject;
      private boolean mIncludeToObject;

      /**
       * Iterate over Item IDs from Git commit messages.
       */
      protected class CCLCommitIterator implements Iterator<ICLCommitContainer>
      {
         private Iterator<RevCommit> mRevIterator;
         private CCLGitCommitContainer mNextItem;
         private boolean mFinished;

         /**
          * Constructor. Remember RevCommit iterator.
          */
         public CCLCommitIterator(Iterator<RevCommit> it)
         {
            this.mRevIterator = it;
            this.mFinished = false;
            this.mNextItem = null;
         }

         /**
          * Check, if there is another Item ID in a commit message.
          */
         @Override
         public boolean hasNext()
         {
            while (mRevIterator.hasNext() && (mNextItem == null) && (!mFinished))
            {
               RevCommit commit = mRevIterator.next();
               CCLDescriptionParser parser = new CCLDescriptionParser(COMMIT_TEMPLATE);
               this.mNextItem = parser.parse(commit.getFullMessage());
               if (this.mNextItem != null)
               {
                  this.mNextItem.setCommitTime(commit.getCommitTime());
                  ObjectId id = commit.getId();
                  // use the first 4 bytes of the hash as identifier
                  this.mNextItem.setHash(id.getName().substring(0, HASH_LEN));
               }

               if (CCLCommitIterable.this.mToObject != null && commit.getId().equals(CCLCommitIterable.this.mToObject))
               {
                  this.mFinished = true;
                  if (!CCLCommitIterable.this.mIncludeToObject)
                  {
                     this.mNextItem = null;
                  }
               }
            }

            return (this.mNextItem != null);
         }

         /**
          * Provide next Item ID.
          */
         @Override
         public CCLGitCommitContainer next()
         {
            CCLGitCommitContainer temp = this.mNextItem;
            this.mNextItem = null;
            return temp;
         }

         /**
          * No implementation available.
          */
         @Override
         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      }

      /**
       * Constructor. No end object
       */
      CCLCommitIterable(ObjectId fromObject)
      {
         this.mFromObject = fromObject;
         this.mToObject = null;
         this.mIncludeToObject = true;
      }

      /**
       * Constructor. Remember repository and TAG ObjectId.
       */
      CCLCommitIterable(ObjectId fromObject, ObjectId toObject, boolean includeToObject)
      {
         this.mFromObject = fromObject;
         this.mToObject = toObject;
         this.mIncludeToObject = includeToObject;
      }

      /**
       * Provide iterator object. Note: Error handling problem. Cannot add
       * exception to predefined interface methods. Therefore a runtime
       * exception is used.
       */
      @Override
      public Iterator<ICLCommitContainer> iterator()
      {
    	   boolean includeMergeCommits = Boolean.parseBoolean(SCLSettings.get(CCLXMLConstants.XML_INCLUDE_MERGE_COMMITS));
    	   LogCommand log = mGit.log();

    	   if (!includeMergeCommits)
    	   {
    	      log.setRevFilter(RevFilter.NO_MERGES);
    	   }    	 
    	 
         try
         {
            if (mToObject == null)
            {
               return new CCLCommitIterator(log.add(this.mFromObject).call().iterator());
            }
            else
            {
               return new CCLCommitIterator(log.addRange(this.mToObject, this.mFromObject).call().iterator());
            }
         }
         catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e)
         {
            throw new CCLVersionControlRuntimeException(getExceptionText(e) + e);
         }
      }

      private String getExceptionText(Exception e)
      {
         String exceptionText;
         if (NoHeadException.class.isInstance(e))
         {
            exceptionText = "Git: Not on Head! ";
         }
         else if (GitAPIException.class.isInstance(e))
         {
            exceptionText = "Git: API error! ";
         }
         else if (MissingObjectException.class.isInstance(e))
         {
            exceptionText = "Git: Missing object error! ";
         }
         else if (IncorrectObjectTypeException.class.isInstance(e))
         {
            exceptionText = "Git: Object type is incorrect! ";
         }
         else
         {
            exceptionText = "Git: error!";
         }
         return exceptionText;
      }
   }

   /**
    * Constructor. Initialize configuration data.
    */
   public CCLGitHandler(CCLGitConfig config) throws CCLVersionControlException
   {
      this.mPathToRepo = config.getPath();
      this.mGit = null;
      open();
   }

   /**
    * Open repository. Has to be called before querying any information.
    */
   protected void open() throws CCLVersionControlException
   {
      File file = new File(mPathToRepo);
      try
      {
         this.mGit = Git.open(file);
      }
      catch (IOException e)
      {
         throw new CCLVersionControlException(IO_ERROR_EXCEPTION + e);
      }
   }

   /**
    * Free resources.
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
    * Get current branch.
    *
    * @return branch name.
    */
   public String getCurrentBranch() throws CCLVersionControlException
   {
      try
      {
         Repository repo = mGit.getRepository();
         return repo.getBranch();
      }
      catch (IOException e)
      {
         throw new CCLVersionControlException(IO_ERROR_EXCEPTION + e);
      }
   }

   /**
    * Get the TAG the HEAD is currently synched to. If multiple TAGs are on the
    * same commit hash, the latest known TAG will be selecteds.
    * 
    * @throws CCLVersionControlException
    */
   public String getLocalTag() throws CCLVersionControlException
   {
      try
      {
         String currentTag = null;
         Repository repo = mGit.getRepository();
         String currentBranch = repo.getBranch();
         String tagCandidate = Git.wrap(repo).describe().setTarget(ObjectId.fromString(currentBranch)).call();
         for (Ref ref : mGit.tagList().call())
         {
            if (ref.getName().contains(tagCandidate))
            {
               currentTag = tagCandidate;
            }
         }
         return currentTag;
      }
      catch (java.lang.IllegalArgumentException e)
      {
         throw new CCLVersionControlException("Repository is not on a tag. " + e);
      }
      catch (IOException | GitAPIException | RuntimeException e)
      {
         throw new CCLVersionControlException(e);
      }
   }

   /**
    * Get latest TAG as Ref object.
    *
    * @throws CCLVersionControlException
    */
   protected Ref getLatestTag() throws CCLVersionControlException
   {
      try
      {
         Ref latestTag = null;
         for (Ref ref : mGit.tagList().call())
         {
            latestTag = ref; // the last entry is the newest tag
         }
         return latestTag;
      }
      catch (GitAPIException e)
      {
         throw new CCLVersionControlException("Error while processing tags. " + e);
      }
   }

   /**
    * Get all TAGs as Ref list.
    *
    * @return list of Refs.
    * @throws CCLVersionControlException
    */
   protected List<Ref> getAllTags() throws CCLVersionControlException
   {
      try
      {
         return mGit.tagList().call();
      }
      catch (GitAPIException e)
      {
         throw new CCLVersionControlException("Error while processing tags." + e);
      }
   }

   /**
    * Check if the current HEAD is synched to a TAG.
    *
    * @return the TAG or null if it is not synched to a TAG
    * @throws CCLVersionControlException
    */
   public CCLGitTagContainer isHeadTag() throws CCLVersionControlException
   {
      CCLGitTagContainer latestTagContainer = null;

      Repository repo = mGit.getRepository();
      try (RevWalk walk = new RevWalk(repo))
      {
         ObjectId head = repo.resolve(GIT_HEAD);

         List<Ref> tagList = getAllTags();
         if (tagList != null)
         {
            for (Ref tag : tagList)
            {
               // Get commit of this tag
               ObjectId targetObject = tag.getObjectId();

               Ref peeledRef = repo.getRefDatabase().peel(tag);
               if (peeledRef.getPeeledObjectId() != null)
               {
                  targetObject = peeledRef.getPeeledObjectId();
               }

               // Are HEAD and TAG identical?
               if (!targetObject.equals(head) && latestTagContainer != null)
               {
                  break; // TAG found -> exit method
               }
               else if (targetObject.equals(head))
               {
                  RevTag revTag = walk.parseTag(tag.getObjectId());
                  latestTagContainer = new CCLGitTagContainer(revTag.getTagName(), revTag.getTaggerIdent().getWhen(),
                        revTag.getId().getName(), targetObject.getName());
               }
            }
         }
      }
      catch (IOException e)
      {
         throw new CCLVersionControlException("Error opening repository: " + this.mPathToRepo + " " + e);
      }

      return latestTagContainer;
   }

   /**
    * Find all commits starting from current HEAD.
    */
   public Iterable<ICLCommitContainer> getAllCommitsIterator() throws CCLVersionControlException
   {
      CCLCommitIterable iterable = null;
      try
      {
         Repository repository = mGit.getRepository();
         iterable = new CCLCommitIterable(repository.resolve(GIT_HEAD));
      }
      catch (IOException e)
      {
         throw new CCLVersionControlException("Error opening repository: " + this.mPathToRepo + " " + e);
      }

      return iterable;
   }

   /**
    * Find commits made after latest TAG.
    */
   public Iterable<ICLCommitContainer> getRangeIterator(String idFrom, String idTo) throws CCLVersionControlException
   {
      if (!ObjectId.isId(idFrom))
      {
         throw new CCLVersionControlException("From-Id: Invalid SHA1");
      }

      if (!ObjectId.isId(idTo))
      {
         throw new CCLVersionControlException("To-Id: Invalid SHA1");
      }

      ObjectId oidFrom = ObjectId.fromString(idFrom);
      ObjectId oidTo = ObjectId.fromString(idTo);

      CCLCommitIterable iterable = null;

      iterable = new CCLCommitIterable(oidFrom, oidTo, true);

      return iterable;
   }
}
