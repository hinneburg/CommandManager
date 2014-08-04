package cc.commandmanager.core.integrationtests;

import cc.commandmanager.core.Command;
import cc.commandmanager.core.Context;
import cc.commandmanager.core.ResultState;
import cc.commandmanager.core.SimpleCommand;

import java.util.List;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with no dependencies. {@link #execute(cc.commandmanager.core.Context)} will bind a {@link Class} object
 * corresponding to this class to the context. A {@link ClassCastException} will be thrown if the context does not have
 * a {@link java.util.List<Class<? extends  cc.commandmanager.core.Command >>}. The list should be bound to the context with the key, specified in the
 * {@link cc.commandmanager.core.integrationtests.CommandManagerIntegrationTest}.
 */
public final class GroupCSecond extends SimpleCommand {

    @SuppressWarnings("unchecked")
    @Override
    public ResultState execute(Context context) {
        ((List<Class<? extends Command>>) context.get(CommandManagerIntegrationTest.EXECUTED_COMMANDS)).add(this
                .getClass());
        return ResultState.success();
    }

}
