# Preparation

## Pre-requisites

 * Java 22 (for 0.6.0 branch onwards)
 * MongoDB (current version) if using  external MongoDB (recommended)

## Preparation

### Prepare Workspace

 * Create a new *Maven Project* called `_run` in your workspace. The pom must have *packaging* of *jar*.
 * Paste the following contents in the `pom.xml` and adjust the version of `jadaptive-boot` for current requirements.
 
 ```xml
 <project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.jadaptive</groupId>
    <artifactId>_run</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Run Project</name>
    <packaging>jar</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.jadaptive</groupId>
            <artifactId>jadaptive-boot</artifactId>
            <version>0.6.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-log4j2</artifactId>
            <version>3.3.13</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <release>22</release>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>libs-snapshots-local</id>
            <snapshots />
            <releases>
                <enabled>false</enabled>
            </releases>
            <url>https://artifactory.jadaptive.com/libs-snapshots-local/</url>
        </repository>
    </repositories>
</project>
 ```
 
### Extension Setup

The primary file for configuration of how to obtain or locate *Plugins* is handled by the `repositories` file. Create this file in the root of your `_run` project.

```
AppBuilder .
AutoUpdate

# The below is all on one line
MavenRepository https://artifactory.jadaptive.com/libs-snapshots-local YOUR_ARTIFACTORY_USERNAME YOUR_ARTIFACTORY_PASSWORD

Install jadaptive-jaul
Install jadaptive-licensing
Install jadaptive-alert-centre
Install jadaptive-ssh-synergy
Install jadaptive-dashboard
Install jadaptive-email
Install jadaptive-templates
Install jadaptive-default-resources
```

*In the above `MavenRepository` line, you will need to use your [https://artifactory.jadaptive.com](Artifactory) username and password.*

This configuration is a minimum required to be able to run a viable server. From this point, you can add more plugins using the `Install` verb, or if you wish to use a plugin in its source format, see [Developing Plugins](#developing-plugins).

To install all the plugins for a particular product, see [Product Repository Files](#product-repository-files).

The full format for the `Install` verb is `Install [<groupId>:]<artifactId>`. If `groupId` is ommitted, then it will automatically detected as `com.jadaptive` if the artifact ID starts with `jadaptive-*`.  Similiarly, plugins that start with `logonbox-` will use the `com.logonbox` group, and again `sshtools-*` for `com.sshtools`.  If the plugin does not match this pattern, the group ID *must* be provided, e.g. one such plugin is the nodal VPN plugin.

```
Install com.jadaptive:nodal-vpn-server
```

#### Automatic Updates

If you add the `AutoUpdate` verb to the `repositories` file. This will ensure any `Install` extensions are kept up-to-date and using the latest `SNAPSHOT` version available in the repository.

This is at the expense of a slightly slower startup, as every extension is checked on every boot.

### Database

If you are using an external Mongo database, obtain it's URL. For example, if you have installed it locally on your development host, it will likely be `mongodb://localhost:27017`.

Before you start the server for the first time, create the directory `conf.d`, and place a file `database.properties` in side it.

```
mongodb.embedded=false
mongodb.connection=mongodb://blue.southpark.lan:27017
```

You will also likely need to configure Mongo so that transactions are supported. Run the following command using Mongo's command line tool `mongosh`.

```bash
mongosh --port 27017 << MONGO_SCRIPT
use admin
db.runCommand(
   {
     replSetInitiate : {
        _id : 'rs0',
         members : [
             {_id : 0, host : '127.0.0.1:27017'},
         ]
    }
   }
)
MONGO_SCRIPT
```

If you wish to try the embedded database, either remove `database.properties`, or just comment out the above two lines if they exist.

### Server Configuration

It is likely you will be unable to run the server on the default port (443)  without running as an administrator, so to change the port the server listens on, create a file `server.properties` in the `conf.d` directory, and place the following contents in it.

```
server.port=7443
server.ssl=true
```

You now have enough to actually start the server.

## Running The Server

Simply create a new launcher in your IDE to the the class `com.jadaptive.app.Application`, with the following JVM arguments.

```
-Djadaptive.loadPluginArchives=true
-Djadaptive.development=true
```

The UI will be available at `https://localhost:7443`.

## Developing The Core

If you need to work on one of the core JAD framework module, i.e. `jadaptive-api` or `jadaptive-boot`, then simply clone the main repository at https://github.com/ludup/jadaptive-app-builder.git to your workspace and import into your IDE. If you IDE is correctly configured and the version matches, your `_run` project will then automatically run from the source module instead of from pre-built Maven artifacts.

## Developing Plugins

In order to work on a plugin directly from it's source, you must adjust the `repositories` file. For example, say you wanted to work on the `jadaptive-sms-twilio` plugin. This exists in the `jadaptive-2fa` repository, which can be located at https://github.com/ludup/jadaptive-2fa.git. So this is the source repository you must clone.

Whether you are creating a new plugin, or working on an existing one, you will have to clone one of the project repositories. If it a new plugin (or suite of plugins)  that it would not be appropriate to put in any of the existing repositories, you should should discuss it first on `#devtalk`. 

```
cd /path/to/your/workspace
git clone -b origin/0.6.0 https://github.com/ludup/jadaptive-2fa.git
```

Then in the `repositories` file, add the following.

```
GitBase /path/to/your/workspace
GitPlugins jadaptive-2fa

Enable jadaptive-sms-twilio
```

In the above ...

  * `GitBase` will always be the same.
  * There may be multiple `GitPlugins`, each named the same as the root directory of each Git repository.
  * There may be multiple `Enable` verbs, each with a plugin ID that exists in one of the `GitPlugins` roots.

### Creating A New Plugin

If you are creating a brand new plugin, the best advice I can offer at the moment is to copy an existing plugin structure and change the Maven artifact ID, and the `<properties>` in  the `pom.xml`. There will also be a `plugin.properties`. You can manually edit  this, or it will be  automatically generated when plugin meta-data is created (see below).
  
### Generate Developer Plugin Metadata

There is an extra step that must be taken after adjusting the `repositories` or changing the Maven dependencies of any plugin  you might be working on.

In the repository root of the project you have changed, run ..

```
mvn -DskipTests clean install
```

*TODO: Check if we *really* have to use `install`, is `package` not enough?*
  
## Product Repository Files

The content below consists of fragments you can add to your `repositories` file that will `Install` all the appropriate plugins for a particular product.

Copy the entire contents of each and add required verbs for the plugins you wish to work on (see above).

### Nodal VPN Cloud

```
AppBuilder .
MavenRepository https://artifactory.jadaptive.com/libs-snapshots-local <username> <password>

Install jadaptive-builtin-users
Install jadaptive-default-resources
Install jadaptive-dashboard
Install jadaptive-jaul
Install jadaptive-templates
Install jadaptive-alert-centre
Install jadaptive-gravatar
Install jadaptive-watchdog

Install jadaptive-ssh-synergy
Install jadaptive-amazon-route-53
Install jadaptive-yubikey
Install jadaptive-fontawesome-pro
Install logonbox-authenticator-server
Install jadaptive-authentication-policies
Install jadaptive-amcharts
Install jadaptive-totp
Install jadaptive-branding
Install jadaptive-auditing
Install jadaptive-licensing
Install jadaptive-email
Install jadaptive-email-authentication
Install jadaptive-sms
Install jadaptive-sms-authentication
Install jadaptive-phone-verification
Install jadaptive-phone-authentication
Install jadaptive-code-authentication
Install jadaptive-multi-tenancy
Install jadaptive-ssh-server
Install jadaptive-ssh-terminal
Install jadaptive-ssh-keys
Install jadaptive-windows-users
Install logonbox-authenticator-server
Install jadaptive-duo
Install jadaptive-users-google
Install jadaptive-users-microsoft
Install jadaptive-s3-compatible-storage
Install jadaptive-alert-messages
Install jadaptive-ssh-proxy
Install jadaptive-ssh-devices

Install com.jadaptive:nodal-vpn-server
Install com.jadaptive:nodal-vpn-theme
Install com.jadaptive:nodal-vpn-cloud
```


### Nodal VPN On Prem

```
AppBuilder .
MavenRepository https://artifactory.jadaptive.com/libs-snapshots-local <username> <password>

Install jadaptive-builtin-users
Install jadaptive-default-resources
Install jadaptive-dashboard
Install jadaptive-jaul
Install jadaptive-templates
Install jadaptive-alert-centre
Install jadaptive-gravatar
Install jadaptive-watchdog

Install jadaptive-ssh-synergy
Install jadaptive-amazon-route-53
Install jadaptive-yubikey
Install jadaptive-fontawesome-pro
Install logonbox-authenticator-server
Install jadaptive-authentication-policies
Install jadaptive-amcharts
Install jadaptive-totp
Install jadaptive-branding
Install jadaptive-auditing
Install jadaptive-licensing
Install jadaptive-email
Install jadaptive-email-authentication
Install jadaptive-sms
Install jadaptive-sms-authentication
Install jadaptive-phone-verification
Install jadaptive-phone-authentication
Install jadaptive-code-authentication
Install jadaptive-multi-tenancy
Install jadaptive-ssh-server
Install jadaptive-ssh-keys
Install jadaptive-ssh-terminal
Install jadaptive-windows-users
Install logonbox-authenticator-server
Install jadaptive-duo
Install jadaptive-users-google
Install jadaptive-users-microsoft
Install jadaptive-s3-compatible-storage
Install jadaptive-alert-messages
Install jadaptive-ssh-proxy
Install jadaptive-ssh-devices

Install com.jadaptive:nodal-vpn-server
Install com.jadaptive:nodal-vpn-theme
Install logonbox-vpn-server
```
  
# Password Express

```
AppBuilder .
MavenRepository https://artifactory.jadaptive.com/libs-snapshots-local <username> <password>

Install jadaptive-builtin-users
Install jadaptive-default-resources
Install jadaptive-dashboard
Install jadaptive-jaul
Install jadaptive-templates
Install jadaptive-alert-centre
Install jadaptive-gravatar
Install jadaptive-watchdog

Install logonbox-express
Install logonbox-authenticator-server
Install jadaptive-ssh-synergy
Install jadaptive-certificate-manager
Install jadaptive-ssh-keys
Install jadaptive-ssh-server
Install jadaptive-amcharts
Install jadaptive-auditing
Install jadaptive-branding
Install jadaptive-email
Install jadaptive-licensing
Install jadaptive-multi-tenancy
Install jadaptive-windows-users
Install jadaptive-windows-login
Install jadaptive-yubikey
Install jadaptive-s3-compatible-storage
Install jadaptive-authentication-policies
Install jadaptive-fontawesome-pro
Install jadaptive-code-authentication
Install jadaptive-email-authentication
Install jadaptive-phone-verification
Install jadaptive-phone-authentication
Install jadaptive-sms-authentication
Install jadaptive-sms
Install jadaptive-totp
Install jadaptive-sms-twilio
Install jadaptive-password-breach-checker
Install jadaptive-duo
```
