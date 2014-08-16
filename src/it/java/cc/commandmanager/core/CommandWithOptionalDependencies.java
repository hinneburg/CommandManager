package cc.commandmanager.core;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public class CommandWithOptionalDependencies implements Command {
    @Override
    public ResultState execute(Context context) {
        return ResultState.success();
    }

    @Override
    public Set<String> getBeforeDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getAfterDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalBeforeDependencies() {
        return Sets.newHashSet("Command in another project");
    }

    @Override
    public Set<String> getOptionalAfterDependencies() {
        return Sets.newHashSet("Command in yet another project");
    }
}
