## CommandManager

### Description

A lean implementation of a command and chain of responsibility pattern mixture. The command manager enables you to easily create applications that are customizable during run time. Commands and their dependencies are configured using an XML catalog and instantiated using reflection when they are executed.

### Usage

#### Command Execution

Commands are specified in an XML file called *catalog*. Every command in the catalog must correspond to a Java class available in the class path during run time. A `CommandManager` object is responsible for the execution of a command graph.

The following three lines of code contain a simple usage example that loads a command graph from a catalog and executes all commands:
```java
CommandGraph commandGraph = CommandGraph.fromXml("catalog.xml");
CommandManager commandManager = new CommandManager(commandGraph);
commandManager.executeAllCommands();
```

#### XML Catalog

A catalog XML file contains a list of commands. Dependencies are currently specified in the command implementation, and not in the Catalog. Each command node is required to have a command name and a class name, which corresponds to the fully qualified Java class name.

Here is a sample catalog containing three commands:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog>
	<command className="my.package.DummyCommand1" name="Command1" />
	<command className="my.package.DummyCommand2" name="Command2" />
	<command className="my.package.DummyCommand3" name="Command3" />
</catalog>
```

#### Command Implementation

A command implementation needs to implement the `Command` interface. The interface has a method called `execute(Context)`, which implements all operations that need to be done by the command. The given context will be used to read and write data which are interchanged with other commands.

Dependencies between commands are currently specified by four other methods: `getBeforeDependencies()`, `getAfterDependencies()`, `getOptionalBeforeDependencies()`, and `getOptionalAfterDependencies()`. All these methods return a set of command names. Before-dependencies incorporate all commands that need to be executed before the current command. After-dependencies are required to be executed after the current command. Currently, optional dependencies have the effect that their absence at run time does not cause the catalog XML loading to crash.

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
