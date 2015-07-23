# kvsrv

a simple persisted key/value database using any JVM-based language/framework.

## Constraints
 * Must persist stored data as a single file to local disk, and be able to load it again after restart Max key size 100 bytes
 * Max value size is 1 000 000 bytes
 * The file on disk used by the database must never exceed 25 000 000 bytes
 * The server must accept at least 10 concurrent connections

## Network protocol
Server listens for tcp connections on arbitrary port.
When a connection is established, the client may send commands in the form of:

    <command name><TAB><key name><TAB><payload size><NEWLINE><payload bytes>

For certain commands, there is no payload (payload size = "0"), Everything up to the newline must be ascii. Payload is an arbitrary sequence of bytes.
When a command is received, the server must respond with:

    <status><TAB><payload size><NEWLINE><payload bytes>

Status should be ascii string "ok" if everything was as expected, or an error message (ascii string without tab chars). For each connection, the client must wait for the server to reply before sending another command


## How to build and run
Prerequisites:

 * JDK 1.7
 * Maven 3

clone this repo and run

    mvn clean install

Start the server with default options: 

    java -jar target/kvsrv-1.0-SNAPSHOT.jar
    
the server will listen on port 3434, have 10 parallel listener threads and use my.db as the storage file.    

For more info on specifying these options, please run:

    java -jar target/kvsrv-1.0-SNAPSHOT.jar -h
    

## Implementation notes
The core design of the KV server is based around [netty](http://netty.io/) and [MapDB](http://www.mapdb.org/). This 
will also reflect a bit of the capabilities. To be able to implement a fully functional product, within the given time 
frame requires you to heavily rely on third party libraries.

Some of the original requirements are not met. Partially due to lack of time but also in some cases, due to the lack 
of support for the requirement in the given third party lib. Given more time, these issues could have been worked
around or requirements changed. The current status can be considered a MVP (Minimum Viable Product).

## Missing requirements and other improvements
 
 * Max file size for DB
 * Max key size
 * Stats(num_connections) in not implemented
 * Logging is not working
 * Timeout handling and security. If the protocol is misused, the server can easily crash or freeze.
 * Memory and performance. No profiling is done, so no optimizations are done. All hope of a good performance is put on
   the third party libraries.
 * Error handling is not good enough. For example, fatal errors and logic signaling must be separated. Now there's a very
   optimistic catch 'em all-approach.
 * Testing. Apart from the unit tests and the integration test suite, very little time has been spend on testing the product.
 * And probably a million more things that I can think of right now.... ;)

There is also a number of TODO:s in the code, indicating some of the areas that probably should need some more improvement.
 

## other assorted, random notes
     
 * Key in request protocol doesn't always apply, like the stats command
 * Strange requirements on KV store? (max file size etc)
 * String based protocol but not encoding. Use UTF_8
 * Homegrown K/V store, why not Casandra etc?
 * Low level byte oriented protocol. Why not protobuf et al?
 * All network protocols should (must?) start with a version byte. 
 * Delim based protocols not that common for obvious reasons ...





