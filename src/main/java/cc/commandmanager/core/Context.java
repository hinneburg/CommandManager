package cc.commandmanager.core;

import java.util.HashMap;
import java.util.Map;

import net.sf.qualitycheck.Check;

/**
 * Represents a naming context, which consists of a set of string-to-object bindings. It contains methods for
 * investigating and updating these bindings. The bindings are represented with a map, so all provided methods
 * correspond to the map's equivalents.
 */
public class Context {

	private final Map<String, Object> items;

	public Context() {
		items = new HashMap<String, Object>();
	}

	public Context(Context context) {
		items = new HashMap<String, Object>();
		Check.notNull(context);
		items.putAll(context.items);
	}

	public void put(String name, Object object) {
		items.put(name, object);
	}

	public Object get(String name) {
		return items.get(name);
	}

	public void remove(String name) {
		items.remove(name);
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		items.putAll(map);
	}

	@Override
	public boolean equals(Object context) {
		return context instanceof Context && items.equals(((Context) context).items);
	}

}
