package cc.commandmanager.core.commandimplementations;

import cc.commandmanager.core.*;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Dummy {@link cc.commandmanager.core.Command} implementation with an after dependency on {@link GroupCSecond}. {@link #execute(cc.commandmanager.core.Context)}
 * will bind a {@link Class} object corresponding to this class to the context. A {@link ClassCastException} will be
 * thrown if the context does not have a {@link java.util.List<Class<? extends  cc.commandmanager.core.Command >>}. The list should be bound to the context
 * with the key, specified in the {@link cc.commandmanager.core.CommandManagerIntegrationTest}.
 */
public final class GroupCFirst extends SimpleCommand {

    @SuppressWarnings("unchecked")
    @Override
    public ResultState execute(Context context) {
        ((List<Class<? extends Command>>) context.get(CommandManagerIntegrationTest.EXECUTED_COMMANDS)).add(this
                .getClass());
        return ResultState.success();
    }

    @Override
    public Set<String> getOptionalAfterDependencies() {
        return Sets.newHashSet("GroupCSecond");
    }

}
