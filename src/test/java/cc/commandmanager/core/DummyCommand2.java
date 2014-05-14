package cc.commandmanager.core;

import org.apache.commons.chain.Context;

public final class DummyCommand2 extends Command {

    @Override
    public void specialExecute(Context context) {
	System.err.println("DummyCommand2 was called.");
    }

    @Override
    public void addDependencies() {
	beforeDependencies.add("DummyCommand1");
    }
}
