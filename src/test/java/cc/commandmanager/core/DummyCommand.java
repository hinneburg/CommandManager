package cc.commandmanager.core;

public class DummyCommand extends SimpleCommand {

	@Override
	public ResultState execute(Context context) {
		return ResultState.success();
	}

}
