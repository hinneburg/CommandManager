package cc.commandmanager.core;

import org.apache.commons.chain.Context;

public final class DummyCommand5 extends Command {

    @Override
    public void specialExecute(Context context) {
	System.err.println("DummyCommad5 was called.");
    }
}
