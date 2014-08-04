package cc.commandmanager.core.integrationtests;

import cc.commandmanager.core.Command;
import cc.commandmanager.core.Context;
import cc.commandmanager.core.ResultState;
import cc.commandmanager.core.SimpleCommand;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with an after dependency on {@link cc.commandmanager.core.integrationtests.GroupASecond}. {@link #execute(cc.commandmanager.core.Context)}
 * will bind a {@link Class} object corresponding to this class to the context. A {@link ClassCastException} will be
 * thrown if the context does not have a {@link List<Class<? extends  cc.commandmanager.core.Command >>}. The list should be bound to the context
 * with the key, specified in the {@link cc.commandmanager.core.integrationtests.CommandManagerIntegrationTest}.
 */
public final class GroupAFirst extends SimpleCommand {

    @SuppressWarnings("unchecked")
    @Override
    public ResultState execute(Context context) {
        ((List<Class<? extends Command>>) context.get(CommandManagerIntegrationTest.EXECUTED_COMMANDS)).add(this
                .getClass());
        return ResultState.success();
    }

    @Override
    public Set<String> getAfterDependencies() {
        return Sets.newHashSet("GroupASecond");
    }

}
