# Shridoop


### General Information
<p>
This is an attempt to recreate a resilient distributed file-system like Apache's HDFS. This was undertaken 
to try to better understand both a network-based file-system, and distributed-networks as a whole.

In order to achieve its resiliency, the file-system utilizes both regular heartbeats to detect failures, and 
Reed-Solomon code algorithms to repair corruptions upon detection. 

It should be noted that this was developed locally, without access to a network 
cluster. The system still leverages TCP and an event-driven API to communicate
between logically independent nodes. The 'Distributed' part is simulated, with each node 
having its own local filesystem, given via a relative home path specified in the 'chunkServers' config file.
A minimum (if any, really) amount of changes should be necessary to convert this 
to a true local cluster.

As a side note, the name comes combining an awesome professor who taught me about all of this stuff during my last year 
in college, with the software this repo is attempting to replicate some features of (HDFS). A minimum amount of 3rd
                                                                                            party libaries were used, and only to simplify extra stuff like terminal auto-complete, and string formatting.
</p>

---

### Requirements
-  Java 8+
-  Linux or Unix-Like ([Cygwin](https://www.cygwin.com/), [MinGW](https://www.mingw-w64.org/), etc) terminal with [Tmux](https://github.com/tmux/tmux/wiki) installed
- [Maven](https://maven.apache.org/)  
  
Note: Any operating system should work given the requirements are fulfilled, but it hasn't been tested on OS X

---
### Setup

1.  Clone the Repository
2.  While inside the Distributed_File_System directory, run `maven compile` to compile and bundle into a fat jar
inside of `./target`
3.  Edit `chunkServers_[OS]`. File line format: `[serverName] [Port] [dynamicPath]`  
    Note: The start-up script `terminal_setup.sh` uses this file
4.  `java -cp /target/Distributed_File_System-1.0.jar fileSystem.node.Controller [PORT]`
<br><br>

--- 
### Screenshots

##### Controller  
![](image/terminal_controller.png?raw=true)

##### Client  
![](image/terminal_client.png?raw=true)

##### ChunkServer  
![](image/terminal_chunk_server_yellow.png?raw=true)

##### Filesystem  
![](image/filesystem_result.png?raw=true)