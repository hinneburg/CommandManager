## CommandManager

### Description

A lean implementation of a command and chain of responsibility pattern mixture. The command manager enables you to easily create applications that are customizable during run time. Commands and their dependencies are configured using an XML catalog and instantiated using reflection when they are executed.

### Usage Example

```java
CommandManager commandManager = new CommandManager(CommandGraph.fromXml("catalog.xml"));
commandManager.executeAllCommands();
```

### Installation

To install the CommandManager you can add it as a maven dependency. Until the binaries are hosted on a public maven repository, it is recommended to clone this repository, checkout the latest release and install it to your local maven repository by executing 

```sh
mvn install
```

### Contribute

In order to contribute you should fork the repository on [GitHub](https://github.com/hinneburg/CommandManager), commit your changes and create a pull request. Please mind the [coding rules](https://github.com/hinneburg/CommandManager/wiki/Coding-Rules) in the wiki.

### Licensing

Please see the file called LICENSE.
