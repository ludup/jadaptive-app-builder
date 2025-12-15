# JAD Clustering Setup Guide

There are two methods of configuring a cluster. You can either take the (almost) completely automated route, or choose to manually configure if there are any special requirements.

## Automatic Configuration (Recommended)

The automatic process only requires that you change a single on the *first* node that will be part of the cluster.

### First Node

First ensure you are using a remote Mongo database that will be accessible by both the first and subsequently configured nodes.

Create a file called `cluster.properties` in  the `conf.d` directory and place a single line it it. If the file already exists, your installation may already be at least partially configured. Refer to manual configuration guide below if that is the case.

```
ha.clusterName=my-cluster-name
```

You can call the cluster anything you want. Restart this node.

### Add More Nodes

Now simply install and start another fresh instance of the same product. Do not make any changes to any configuration files, instead run the setup wizard as normal, and  login as an administrator. 

Then click on the menu item *Administration -> Join Cluster*. You will be asked for the URL of a node that is already on the cluster. This can be load balanced URL, or direct, as long as the SSL certificate is valid. 

You will then be redirected to this cluster node. You must authenticate as an administrator, and then *Approve* the join request when asked to do so.

After a few seconds, you will return back to the originating node and a success message should be displayed. 10 seconds after this, the node will shut itself down.

*The node must be manually started again to complete joining the cluster*

You can then repeat this whole section for as many nodes as are required.

### Detach Node From Cluster

Assuming the node was configured automatically as above, to detach from the cluster you would ..

 * Shut down the products service, e.g. `systemctl stop nodal-cloud`.
 * Delete `conf.d/cluster.properties`. 
 * If the cluster used synchronized local encryption keys (and assuming you kept the backup files), remove `conf/private/secrets` and `conf/private/secrets.pub` and rename `conf/private/secrets.bak` to `conf/private/secrets` and `conf/private/secrets.pub.bak` to `conf/private/secrets.pub`. 
 * Edit `conf.d/database.properties` and set `mongodb.connection` to `mongobdb://localhost:27017` (or 37017 if this a Nodal Cloud VM).
 * Re-enable and start the local `mongod` service if you previously disabled it.
 * Start the products service.

## Manual Configuration

Prepare everything you need to start the manual clustering configuration.

 * An online UUID generator will be useful. https://www.uuidgenerator.net/
 * Create or gain access to a shared Mongo database.
 * Install same product of same version on all required nodes. Set the cluster shared database string using the installer if possible. 
 * Shut down the service while the remainder of  the configuration is applied. `systemctl stop <product>`,  e.g. `systemctl stop nodal-cloud`.

### Database

 * It is recommend the database be configured by the installer, but if you have to set it after the fact, you may change  the `mongodb,connection` property in `conf.d/database.properties`. Also ensure that `.install4j/response.varfile` is also changed, or updates might rewrite the connection string.
 * Ensure `monogodb.embedded` is set to `false`, also in `conf.d/database.properties`.
 
### Encryption Key

Every node must have the same encryption key. There are currently two ways to do this. Either either the built-in file based key, or the  LogonBox key server.

#### Built-In File Based Key

This key is generated on start-up, unless `private.enabled` is set to `false` in any property file. You must ensure the same key is available on all nodes. 

 1. Complete clustering configuration on *one* node.
 1. Start *only* this first node, allow start-up to complete.
 1. Copy the `conf/private` and its contents from the source node to all the other nodes at the same location (replacing any existing contents).
 1. Complete clustering configuration for all the *other* nodes, and start them up too.
 
There are some properties to configure this provider. For example you could set `private.dir`. These paths support Java VFS paths, so potentially (with the right libraries installed) be used for a shared directory for the key. 

#### Use the LogonBox Key Server

Create or amend the file in `conf.d` called `cluster.properties`. Apply the *same* configuration on all nodes in the cluster.

```
keyserver.host=1.2.3.4
keyserver.secret=bigSecretKeyString
keyserver.reference=XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
```

`keyserver.host` must be the IP address or hostname of the key server. `keyserver.secret` must  match the secret on the key server. And `keyserver.reference`  must be a cluster-unique UUID string. By cluster-unique, it is meant that every node in the cluster will use the same reference.

### Distributed Scheduler

The distributed scheduler is enabled by default (NOTE: This will change at some point). But there is some configuration you should do when it is used in a cluster.

Create or amend the `cluster.properties` in `conf.d`.  

```
ha.hostname=cluster-node-1
ha.id=XXXXXXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXX
ha.clusterName=my-cloud-cluster-1
ha.props=jad-cluster.xml

# defaults to number of local cores
#ha.poolThreads=4

# defaults to hostname with random number (depending on JGroups configuration)
#ha.groupName=some-unique-name

# how long (in minutes) to wait for running tasks to 
# complete on shutdown. Defaults to forever
#ha.shutdownTimeout=10
```

Set the `ha.hostname` of the cluster, this may be the IP address, but a name is preferred. If not provided, the current local host name will be used (if it can be detected). 

The `ha.id` is a UUID and must be node unique, i.e. every node should have it's own UUID. If it is not provided, the ID will be generated from the hostname and port. This could potentially be unstable though, so manually setting a UUID is recommended for production services. 

`ha.clusterName` is the name of the cluster the node will be joining. If using for example LAN  broadcast configuration, then any node on a named cluster will be able to communicate with other nodes on  the same named cluster. The default cluster name is the same as `ha.id`, i.e. the node will be in a cluster of it's own.

The `ha.props` property is the gateway to advanced JGroups configuration, which is way beyond the scope of this document. It points to either one of the default well known configuration names, or the full path to a custom configuration file or URL. For now, `jad-cluster.xml` should be OK if the nodes are all on the same well protected private LAN. For any kind of security though, manual configuration will be required. AI will probably help here. 

There are other properties, shown commented out with an explanation above.

## Tidy Up

 * Disable MongoD on all nodes. `systemctl disable mongod && systemctl stop mongod`.
 * Remove unused backed up private and public keys (`secrets.bak` and `secretes.bak.pub` if they exist).

