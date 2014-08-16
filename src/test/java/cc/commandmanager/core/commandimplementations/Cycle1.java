package cc.commandmanager.core.commandimplementations;

import cc.commandmanager.core.Command;
import cc.commandmanager.core.Context;
import cc.commandmanager.core.ResultState;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 *
 * /**
 * {@link cc.commandmanager.core.Command} implementation to induce a circular graph which can be used for test issues.
 * The cycle will be induced as follows:
 * {@linkplain Cycle1} (before) -> {@linkplain Cycle2} (before) ->
 * {@linkplain Cycle3} (before) -> {@linkplain Cycle1}.
 */
public class Cycle1 implements Command {

    @Override
    public ResultState execute(Context context) {
        return ResultState.success();
    }

    @Override
    public Set<String> getBeforeDependencies() {
        return Sets.newHashSet("Cycle2");
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
