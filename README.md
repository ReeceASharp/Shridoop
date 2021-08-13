# Distributed_File_System


### General Information
<p>
This is an attempt to recreate a resilient distributed file-system like Apache's HDFS. This was undertaken 
to try to better understand both HDFS, and distributed-networks as a whole.

In order to achieve its resiliency, the file-system utilizes both regular heartbeats to detect failures, and 
Reed-Solomon code algorithms to repair corruptions upon detection. 

However, it should be noted that this was development all locally, without access to a network 
cluster. With that being said, it still leverages TCP and an event-driven API to communicate
between independent nodes. Due to having limited resources, the 'Distributed' part will be simulated, with each node 
having its own local filesystem, given via a relative home path specified in the 'chunkServers' config file.

If this ever was converted to utilize a network cluster, a minimum amount of changes should be necessary to convert this 
to a true local cluster.
</p>

---

### Requirements
- Java 8
- Linux or Unix-Like (Cygwin, MinGW, etc) terminal with [Tmux](https://github.com/tmux/tmux/wiki) installed
- [Maven](https://maven.apache.org/)  
  
Note: Any operating system should work given the requirements are fulfilled, but it hasn't been tested on OS X

---
### Setup

1. Clone the Repository
2. While inside the Distributed_File_System directory, run `maven compile` to compile and bundle into a fat jar
inside of `./target`
3. Edit 'chunkServers_[OS]'. File format: `[serverName] [Port] [dynamicPath]`  
    Note: The start-up script `terminal_setup.sh` uses this file
4. 'java -cp /target/Distributed_File_System-1.0.jar fileSystem.node.Controller [PORT]' 
<br><br>