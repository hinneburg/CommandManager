package cc.commandmanager.core;

import org.apache.commons.chain.Context;

public final class DummyCommand3 extends Command {

    @Override
    public void specialExecute(Context context) {
	System.err.println("DummyCommand3 was called.");
    }

    @Override
    public void addDependencies() {
	beforeDependencies.add("DummyCommand1");
	optionalBeforeDependencies.add("DummyCommand2");
    }

}
