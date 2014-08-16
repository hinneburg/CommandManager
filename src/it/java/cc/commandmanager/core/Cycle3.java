package cc.commandmanager.core;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 *
 * /**
 * {@link cc.commandmanager.core.Command} implementation to induce a circular graph which can be used for test issues.
 * The cycle will be induced as follows:
 * {@linkplain cc.commandmanager.core.Cycle3} (before) -> {@linkplain cc.commandmanager.core.Cycle3} (before) ->
 * {@linkplain cc.commandmanager.core.Cycle3} (before) -> {@linkplain cc.commandmanager.core.Cycle3}.
 */
public class Cycle3 implements Command {

    @Override
    public ResultState execute(Context context) {
        return ResultState.success();
    }

    @Override
    public Set<String> getBeforeDependencies() {
        return Sets.newHashSet("Cycle1");
    }

    @Override
    public Set<String> getAfterDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalBeforeDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalAfterDependencies() {
        return Collections.emptySet();
    }
}
