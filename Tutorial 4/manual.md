# Remote Monitoring and Configuration
-----

# Load Balancer
A load balancer's main job is distribute the requests across the servers. It does this in two ways:
- Round Robin: requests are distributed evenly across the servers, does not take into consideration request type
- Fair En-queuing: requests are distributed according to the load of the servers

## HAProxy
HAProxy, is highly configurable server for load balancing, content caching and can act as a reverse proxy. It is the leading load balancing software in the industry. HAProxy is generally found in the distributions repository, MacOS users can download HAProxy using brew:

```sh
brew install haproxy
```

### Logging

The default configuration file for HAProxy is found in:
> /etc/haproxy/haproxy.cfg

The default configuration file supplied by Arch Linux, *chroots* the server, that is it runs in a sandboxed environment to mitigate against attacks. Thus, HAProxy has no access the `outside` world and can only access a limited set of files. HAProxy ***does not*** log to the *stdout* stream, and logs to files instead, being in a chroot environment the host OS has no means of access such logs, thus the logs need to be redirected to a *Unix Socket*.

### Unix Sockets
A Unix domain socket or IPC socket (inter-process communication socket) is a data communications endpoint for exchanging data between processes executing on the same host operating system. They are similar in functionality to named pipes however, they also support transmission of a reliable stream of bytes (SOCK_STREAM, similar to TCP). Additionally, they support ordered and reliable transmission of datagrams (SOCK_SEQPACKET, similar to SCTP), or unordered and unreliable transmission of datagrams (SOCK_DGRAM, similar to UDP).

The API for Unix domain sockets is similar to that of an Internet socket, but rather than using an underlying network protocol, all communication occurs entirely within the operating system kernel. Unix domain sockets use the file system as their address name space. Processes reference Unix domain sockets as a file, so two processes can communicate by opening the same socket.

*Systemd* based linux distributions can forward the logs to *Journald* the default logging mechanism supplied with systemd. This can be done by changing the log attribute in the global section to be:
> log /run/systemd/journal/dev-log local0

The file `/run/systemd/journal/dev-log` is the general path for journald's socket on linux.

### Proxies
HAProxy's interface is split up into two main sections:
- Front End: this describes a set of listening sockets accepting client connections
- Back End: describes a set of servers to which the proxy will connect to forward incoming connections
- Listen: this is an optional section, which defines a complete proxy with its frontend and backend
parts combined in one section. It is generally useful for TCP-only traffic

more information on their setup can be found [here](https://cbonte.github.io/haproxy-dconv/1.8/configuration.html#4).

### Statistics
HAProxy offers two ways to access its statistics: using a unix socket or a web interface. To use the web interface, a section must be instantiated for it as follows:
```
listen stats # Define a listen section called "stats"
  bind :8000 # Listen on localhost:8000
  mode http
  stats enable  # Enable stats page
  stats hide-version  # Hide HAProxy version
  stats realm Haproxy\ Statistics  # Title text for popup window
  stats uri /haproxy_stats  # Stats URI
  stats auth USERNAME:PASSWORD  # Authentication credentials
```

The *USERNAME* and *PASSWORD* fields in the last line are by choice, you choose their values. To enable statistics to a socket:
> stats socket /var/run/haproxy.sock mode 600 level admin

This will use the file /var/run/haproxy.sock as a socket with permission level 600 on the filesystem running as an admin, more information on permission levels is [here](https://www.ics.uci.edu/computing/linux/file-security.php), more details for logging to sockets can be found [here](https://cbonte.github.io/haproxy-dconv/1.8/configuration.html#3.1-stats%20socket) and [here](https://cbonte.github.io/haproxy-dconv/1.8/management.html#9.3).
