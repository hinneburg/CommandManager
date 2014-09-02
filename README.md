## CommandManager

### Description

A lean implementation of a command and chain of responsibility pattern mixture. The command manager enables you to easily create applications that are customizable during run time. Commands and their dependencies are configured using an XML catalog and instantiated using reflection when they are executed.

### Usage

Commands are specified in an XML file called *catalog*. Every command in the catalog must correspond to a Java class available in the class path during run time. A `CommandManager` object is responsible for the execution of command graphs.

The following three lines of code contain a simple usage example that loads a command graph from a catalog and executes all commands:
```java
CommandGraph commandGraph = CommandGraph.fromXml("catalog.xml");
CommandManager commandManager = new CommandManager(commandGraph);
commandManager.executeAllCommands();
```

### Installation

To install the CommandManager you can add it as a maven dependency. Until the binaries are hosted on a public maven repository, it is recommended to clone this repository, checkout the latest release and install it to your local maven repository by executing 

```sh
mvn install
```

### Contribute

In order to contribute you should fork the repository on [GitHub](https://github.com/hinneburg/CommandManager), commit your changes and create a pull request. Please mind the [coding rules](https://github.com/hinneburg/CommandManager/wiki/Coding-Rules) in the wiki.

Feel free to submit bugs, feature requests or suggestions at any time. Best is to create a GitHub issue for this.

### Licensing

Please see the file called LICENSE.
