# Gitlet Design Document
Author: David Babazadeh

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

###Repository Class
* head pointer to current commit 
* ArrayList<Commit>() branches: symbolic references to nodes (eg cool beans)
* root of commit tree -> Contains and manages commits and staging blobs/files.

###Commit Class
This class represents a Commit instance that contains metadata specific to the commit.
* Timestamp
* Author
* Log message
* Version 
* parent Commits (or is this built in with tree data structure representing branch of commits)
List of blob/file names (hashes) which are stored in some directory

###Blob Class
This class represents a Blob Instance. It contains the contents of files
Instance Variables:
* File Name
* Version 




## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

Main Class:

Repository Class:
Add/Staging method
Commit method 
Adds a node to the child of the cur
Init method
Remove/RM method
Log method
Global-log method
Find method
Status method 
Checkout method
Branch method
Rm-branch method (remove branch)
Reset branch
Merge method
Commit Class
Commit constructor method that contains field information from parent commit node, including the parent node’s list of blobs. Also the new metadata is contained in this commit instance, such as the time. This constructor also creates new blobs for different commits whose files have been modified/created.
Blob Class implements serializable
Imported hashCode method


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

java gitlet.Main init -> save new .gitlet directory in current directory (if possible)
java gitlet.Main add [file] -> save this file to stage directory (.gitlet/stage/additions).

java gitlet.Main commit -m [message] -> creates folder in .gitlet/log using its hash as a name, new version blobs are stored in .gitlet/blobs, removes pointers from removed blobs (internally)
java gitlet.Main merge [branchname] -> add new commit file in .gitlet/stage with same blobs overwritten.


## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

Attached as png.

