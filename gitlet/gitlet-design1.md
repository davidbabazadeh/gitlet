# Gitlet Design Document
Author: David Babazadeh

## 1. Classes and Data Structures

### Main Class
just a driver that handles user inputs.

### Repository Class
sets up files to run commands on, keeps track of commit tree and branch pointers
* File head: reference to head commit
* File branches directory
* File commits directory

* head pointer to current commit 
* ArrayList<Commit>() branches: symbolic references to nodes (eg cool beans)
* root of commit tree -> Contains and manages commits and staging blobs/files.

### Commit Class
Different versions of the repository represented by added/modified/deleted blobs,
and previous versions. implements serializable.
* Date Timestamp (meta)
* String Author (meta)
* String Log message 
* parents: (can be represented using list of commits, instead using two parents for simplicity)
  * transient Commit parent1
  * transient Commit parent2 (necessary for merges)
  * String parent1 (hash reference)
  * String parent2 (hash reference)
* 
* TreeMap<String, String> blobs: maps og-filenames to blob hashes
* 
* ! String list of user's filenames (persistence - each containing corresponding hash)
* ! String list of sha1 hashed filenames (persistence - each containing serialized contents)

### Tree?

### Blob Class
Blob instances contain the contents of individual files 
attached to their filename, so they can be distinguished from other files after being hashed.
implements serializable.
* filename
* contents




## 2. Algorithms

#### Main:

#### Repo:
* init(): sets up .gitlet files/directories in users _cwd_, setting head to a newly created initial commit
* add(String[] args): calls add(String filename) on files indicated by each string given (see below).
* add(File file): adds the given file in its current state to the _additions folder_ of the
_staging area_. 
  * details/quirks: overwrites file in staging area if one dwells there with the same name. removes file from
  the _deletions_ folder if it dwells there. removes file from staging area if it is identical to previous commit.
* commit(String msg): instantiates a commit that persists in the _commits_ folder, tracking the current commit
& staged files. new commit message is defined by msg which is surrounded by quotes if multi-worded. abbre. allowed.
  * details/quirks: default commit only tracks its parent's files. next, updates the content from previous commit
  * tracks (added) staged files which its parent did not track. compliment for removed staged files.
  * clears staging area. never modifies cwd. head & current branch moves to new commit.
  * commits are identified by their sha1 hash which includes refs to its blobs/files, parent refs, log msg, & timestamp.
* rm(String[] args): calls rm(String filename) on using files indicated by each string given (see below).
* rm(String filename): unstages file if staged for addition, removes file from cwd & stages removal if file is tracked by 
current Commit.
* logHeadPath(): displays commit info from head to initial commit (backwards)
* logAll(): handles global-log, dispays commit info for all commits in repo by iterating through _commits_.
  * Utils.plainFilenamesIn
* find(String msg): prints all commit id's that have the exact commit message (one per line).
* status(): displays existing branches (marking head with preceding *), staged files, removed files, 
(modded | deleted) && unstaged files, and untracked files (ignoring subdirectories). lexicographic order.
  * plainFilenamesIn... ((name == head) ? "*" : "") + name
* checkout(String[] args): calls checkouts correspondingly
  * checkoutFile(Commit commit, File file): case of " -- filename" or " id -- filename". overwrites
  file in cwd with commit's version of that file.
  * checkoutBranch(String branch): finds branch and copy/replaces of the commit's files, deleting
  the rest. updates head to this branch. clears staging area if checked-out branch != current branch
* addBranch(String symbol): creates symbolic id pointed at head by persisting a file named symbol containing
hash of head in _branches_ folder. does not change head.
* rmBranch(String symbol): removes branch id/pointer; deletes branch file named symbol in _branches_
* checkoutCommit(String id): handles reset. checks out branch then moves current branch head. // is that different?
* merge(String branch):
  * Checking if a merge is unnecessary: split point == given branch; end with msg.
  * checkout simplification: split point == current branch (checkout given branch); end with msg.
  * modded files in given branch (since split) overwrite those in current branch (iff they haven't been modded since split)
  automatically staged
  * files only present at curr branch remain. files only present in given branch are checked out and staged.
  * files present at split, unmodded in current branch, and absent in given branch are removed/untracked
  * files present at split, absent in current branch, and unmodded in current branch remain absent.
  * Determining which files (if any) have a conflict.
  * Representing the conflict in the file: formatted concatenation/join.
  * commit with merge log message and two parents. (terminal) print if there were conflicts

* splitPoint(Commit a, Commit b): searches backwards from nodes to find common ancestor nearest to a.
* compareFile(String filename, Commit given) checks if file was created, modded, or removed since given commit
  * might want alt version with two commits (e.g. split vs given vs current vs cwd)
  * perhaps in commit class
  * perhaps returns an int (or byte or short) indicating options modded vs unmodded
  vs added vs deleted with positive negative values
  grouping mod with delete ( or whatever is convenient)


  
#### Commit:

* saveCommit(File file)
* logHistory()

#### Blob:

* Merge:



## 3. Persistence

* .gitlet/
  * commits
    * sha1-hash-id :: serialized commit
    *  ...
  * branches
    * symbolic-id :: path/sha1-hash-id
  * Untracked
  * Modified
  * staging_area
    * additions
      * cwd-filename :: serialized blob/content
    * deletions
      * cwd-filename ::
  * blobs
    * sha1-hash-id :: serialized blob/content
  * HEAD :: path/sha1-hash-id

* structure list of all the times you record the state of the program or files.
  For example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* which pieces of data are needed across multiple calls to Gitlet.

get file name from blob .../blobs/hash -> readObject -> .filename
get og contents from blob .../blobs/hash -> readObject -> .contents


## 4. Design Diagram

Attached as png.

