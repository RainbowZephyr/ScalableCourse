# Technologies To Build A Distributed System

------

## RPC

RPC (Remote Procedure Call) is an API that allows programs to call methods in other networks, mainly used in C/C++ codes. This method was the first of its kind to allow access to remote elements.  To use RPC, multiple files must be specified:

### date.x

This file specifies the number of the method and the number of the program which must be unique across the network.

```c
// date.x - Specification of remote date and time service bindate() which returns the binary time and date (no args). This file is the input to rpcgen
program DATEPROG { // remote program name (not used)
	version DATEVERS { // declaration of program version number
		long BINDATE(void) = 1; // procedure number = 1
	} = 1; // definition of program version = 1
} = 0x3012225; // remote program number (must be unique)
```

### dateproc.c
This is the file that contains the method that the client will need.

```c
// dateproc.c - remote procedures; called by server stub
#include <stdio.h>
#include <stdlib.h>
#include <rpc/rpc.h>
#include "date.h"
/* return the binary date and time
##################################################################
In Linux: long * bindate_1_svc(void* arg1, struct svc_req *arg2) {
##################################################################
In Dec Unix (MacOS): long * bindate_1() {
##################################################################
*/

long * bindate_1_svc(void* arg1, struct svc_req *arg2) {
	static long timeval; // must be static
	timeval = time((long *) 0);
	return (&timeval);
}
```

### rdate.c

This is the client that will request the method from the RPC server

```c
// rdate.c - client program for remote date service
#include <stdio.h>
#include <rpc/rpc.h>
#include <stdlib.h>
#include "date.h"
int main(int argc, char *argv[]) {
	CLIENT *cl;
	char *server;
	long *lres;

	if (argc != 2) {
		fprintf(stderr, "usage: %s hostname\n", argv[0]);
		exit(1);
	}

	server = argv[1];
	// create client handle
	if ((cl = clnt_create(server, DATEPROG, DATEVERS, "udp")) == NULL) {
		// couldn't establish connection with server
		printf("can't establish connection with host %s\n", server);
		exit(2);
	}

  // first call the remote procedure bindate()
	if (( lres = bindate_1(NULL, cl)) == NULL){
		printf(" remote procedure bindate() failure\n");
		exit(3);
	}

	printf("time on host %s = %ld\n", server, *lres);
	clnt_destroy(cl); /* done with handle */
	return 0;
}
```

To compile the code run the following

```sh
rpcgen date.x #Generate RPC files to be used by server
gcc -c date_clnt.c # RPC client
gcc -c date_svc.c  # RPC server
gcc -c dateproc.c  # method sender
gcc -c rdate.c     # method receiver
gcc -o client date_clnt.o rdate.o # link RPC client with method receiver (our client)
gcc -o server date_svc.o dateproc.o # link RPC server with method server (our server)
```

To test the code, the RPC registry needs to be running, for Archlinux:

```sh
systemctl start rpcbind
```

----

## RMI

Following RPC, came RMI (Remote Method Invocation), this is an API that provides a mechanism to create distributed application in Java. The RMI allows an object to invoke methods on an object running in another JVM.

To implement the RMI, an interface needs to be created that will be implemented by both the server and the client:

### RMIInterface

```java
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

    public String helloTo(String name) throws RemoteException;

}
```

The server needs to extend the UniCastRemoteObject to be able to called remotely

### Server

```java
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerOperation extends UnicastRemoteObject implements RMIInterface{

    private static final long serialVersionUID = 1L;

    protected ServerOperation() throws RemoteException
        super();
    }

    @Override
    public String helloTo(String name) throws RemoteException{
        System.err.println(name + " is trying to contact!");
        return "Server says hello to " + name;
    }

    public static void main(String[] args){

        try {
            Naming.rebind("//localhost/MyServer", new ServerOperation());
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
```

### Client

```java
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;

public class ClientOperation {

    private static RMIInterface look_up;

    public static void main(String[] args)
            throws MalformedURLException, RemoteException, NotBoundException {

        look_up = (RMIInterface) Naming.lookup("//localhost/MyServer");
        String txt = JOptionPane.showInputDialog("What is your name?");

        String response = look_up.helloTo(txt);
        JOptionPane.showMessageDialog(null, response);

    }

}
```

The example above sends the string collected from the client to server by calling the **helloTo** method. To run the example, make sure that the java files are compiled to the .class files, and navigate to that directory from the terminal and start the RMI registry as so:
```sh
rmiregistry
```
Then run the server class then the client class.

-----

## RabbitMQ
Message queues serve as a buffers for requests between applications. RabbitMQ is a messaging queue written in Erlang, a language created for scalable, distributed, fault-tolerant systems and soft-realtime systems.  There are three categroies of realtime applications:
- Hard: missing a deadline is a total system failure
- Firm: infrequent deadline misses are tolerable, but may degrade the system's quality of service. The usefulness of a result is zero after its deadline]
- Soft: the usefulness of a result degrades after its deadline, thereby degrading the system's quality of service


RabbitMQ comes packaged with a plugin manager, for instance to enable the web interface

```sh
#: rabbitmq-plugins enable rabbitmq_management
```

The interface is now running at (http://localhost:15672), the default username and password are both *guest*.

### Sender-Receiver
A basic setup is as follows:

#### Send

```java
package RabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {

  private final static String QUEUE_NAME = "hello";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    String message = "Hello World!";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");

    channel.close();
    connection.close();
  }
}
```

#### Receive

```java
package RabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Recv {

  private final static String QUEUE_NAME = "hello";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
      }
    };
    channel.basicConsume(QUEUE_NAME, true, consumer);
  }
}
```

### Correlation ID

In the case that there are multiple clients connecting to the queue, an ID must be assigned to each request to ensure that each request is sent back to the correct client. This can be done through the use of a correlation ID:

#### Server
```java
Consumer consumer = new DefaultConsumer(channel) {

  @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();

        System.out.println("Responding to corrID: "+
                           properties.getCorrelationId());

        String response = "";

        try {
            String message = new String(body, "UTF-8");
            int n = Integer.parseInt(message);

            System.out.println(" [.] fib(" + message + ")");
            response += fib(n);
        } catch (RuntimeException e) {
            System.out.println(" [.] " + e.toString());
        } finally {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
         // RabbitMq consumer worker thread notifies the RPC server owner thread
            synchronized (this) {
                this.notify();
            }
        }
    }
};
```

#### Client

```java
String corrId = UUID.randomUUID().toString();
System.out.println(corrId);
AMQP.BasicProperties props = new AMQP.BasicProperties
                                     .Builder()
                                     .correlationId(corrId)
                                     .replyTo(replyQueueName)
                                     .build();

channel.basicPublish("",
                     requestQueueName,
                     props,
                     message.getBytes("UTF-8"));

final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {

@Override
public void handleDelivery(String consumerTag,
                           Envelope envelope,
                           AMQP.BasicProperties properties,
                           byte[] body) throws IOException {

    if (properties.getCorrelationId().equals(corrId)) {
        response.offer(new String(body, "UTF-8"));
    }
 }
```

### Acknowledgements
By default RabbitMQ leaves message acknowledgements to the developer to handle, this is to allow the server to remove the message from its queues once it is sure that it has been received and handled by the client. The following line of code handles that:
```java
channel.basicAck(envelope.getDeliveryTag(), false);
```
If an acknowledgement has not been received it will stay in the server and will be sent again to the worker.

### Publish-Subcribe
To send one message to multiple listening parties, a Publish-Subscribe model needs to be utilized.

#### Publisher

``` java
package PublishSubscribe;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class EmitLog {

  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

    String message = getMessage(argv);

    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");

    channel.close();
    connection.close();
  }

  private static String getMessage(String[] strings){
    if (strings.length < 1)
    	    return "info: Hello World!";
    return joinStrings(strings, " ");
  }

  private static String joinStrings(String[] strings, String delimiter) {
    int length = strings.length;
    if (length == 0) return "";
    StringBuilder words = new StringBuilder(strings[0]);
    for (int i = 1; i < length; i++) {
        words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}
```


#### Subscriber
```java
package PublishSubscribe;

import com.rabbitmq.client.*;

import java.io.IOException;

public class ReceiveLogs {
  private static final String EXCHANGE_NAME = "logs";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);
  }
}
```

-----

## Dependency Management In Java

### Maven

Maven is a Java project management tool, used it to manage the dependencies of a project as well as generating a JAR from the project. There is a [maven repository](https://mvnrepository.com/) that contains a wide assortment of JARs.

Maven has 9 tools:
1. Clean: removes previously generated JARs
2. Validate: validate the project is correct and all necessary information is available, any missing JAR is downloaded from the repository
3. Compile: compile the source code of the project
4. Test: test the compiled source code using a suitable unit testing framework. These tests should not require the code be packaged or deployed
5. Package: take the compiled code and package it in its distributable format, such as a JAR.
6. Verify: run any checks on results of integration tests to ensure quality criteria are met
7. Install: install the package into the local repository, for use as a dependency in other projects locally
8. Site: generates project's site documentation
9. Deploy: done in the build environment, copies the final package to the remote repository for sharing with other developers and projects

When defining a Maven project in Eclipse/Intellij, a *GroupID* and *ArtificatID* needs to be chosen, these reference:
- GroupID: will identify the project uniquely across all projects, so we need to enforce a naming schema. It has to follow the package name rules, what that means it that it has to be at least as a controllable domain name control, multiple subgroups can be created

**Example:** org.apache.maven, guc.facebook, guc.facebook.chat-app

- ArtificatID: is the name of the JAR without version. If you created the JAR then any name can be chosen with only lowercase letters and no symbols. If it's a third party JAR use the name of the JAR as it's distributed.

**Example:** maven, commons-math, chat-app

After creating the project, there will be a POM file created, this is what maven uses to control the project. This file specifies, the Java version to use, the dependencies to add and how to structure the project.

```XML
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.6.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
    </plugins>
</build>

<dependencies>
    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongodb-driver-async</artifactId>
        <version>3.5.0</version>
    </dependency>

    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongo-java-driver</artifactId>
        <version>3.5.0</version>
    </dependency>
</dependencies>
```

#### Runnable JARs
To produce runnable JARs in Maven, the *pom.xml* file needs to have the following plugin:
```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>Main</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```
The main class tag should be changed accordingly, the JAR can now be produced by running the *Package* life-cycle in Maven.
