## CommandManager

### Description

A lean implementation of a command and chain of responsibility pattern mixture. The command manager enables you to easily create applications that are customizable during run time. Commands and their dependencies are configured using an XML catalog and instantiated using reflection when they are executed.

### Usage Example

```java
CommandManager commandManager = new CommandManager(CommandGraph.fromXml("catalog.xml"));
commandManager.executeAllCommands();
```



### Licensing

Please see the file called LICENSE.
