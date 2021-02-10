# Distributed_File_System


### General Information
<p>
This is an attempt to recreate a resilient distributed file-system. This was undertaken 
to try to better understand both HDFS, and distributed-networks as a whole.

In order to achieve its resiliency, it will utilize both heartbeats to detect failures, and 
Reed-Solomon codes to repair corruptions upon detection. 

However, it should be noted that this was development all locally, without access to a network 
cluster. With that being said, it still leverages TCP and an event-driven API to communicated 
between independent nodes. In order to do to simulate each node having its own local filesystem,
a relative home path for each node is specified in the 'chunkServers' config file.

If this ever was converted to utilize a network cluster, a minimum amount of changes should be necessary to convert this 
to a true local cluster.
</p>

---

### Requirements
This was written on a Windows 10 distribution running Java 8, but the only thing specific to it is the script
used to create ChunkServers. Currently, it uses the command line to open up a linux-like terminal
(git-bash) and runs a shell script from there. Out-of-the-box linux support is a future feature.
Additionally, the build tool Maven must be installed in order to compile this and to install
a few third party packages.

---
### Setup

**Windows**
<br>Note: Git-bash must be on the system path for initialization script to function correctly


1. Clone the Repository
2. Run 'maven compile jar' to compile and bundle into a fat jar
3. Edit 'chunkServers' with the # of ChunkServers wanted. File format: '[Nickname] [Port] [dynamicPath]'
3. 'java -cp /target/Distributed_File_System-1.0.jar fileSystem.node.Controller [PORT]' 
<br><br>

**Linux**
<br>
Some minor adjustments would need to be made, but no support out of the box
