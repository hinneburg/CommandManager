package cc.commandmanager.core;

import java.util.Map;

import javax.annotation.Nullable;

import net.sf.qualitycheck.Check;

import com.google.common.collect.Maps;

/**
 * Represents a naming context, which consists of a set of string-to-object bindings. It contains methods for
 * investigating and updating these bindings. The bindings are represented with a map, so all provided methods
 * correspond to the map's equivalents.
 */
public class Context {

	private final Map<String, Object> items;

	public Context() {
		items = Maps.newHashMap();
	}

	public Context(Context context) {
		this();
		items.putAll(Check.notNull(context).items);
	}

	public void put(String name, @Nullable Object object) {
		items.put(Check.notNull(name), object);
	}

	/**
	 * Retrieves the value bound to the given key.
	 *
	 * @param name
	 * @return value bound to that key
	 */
	public Object get(String name) {
		return items.get(Check.notNull(name));
	}

	public void remove(String name) {
		items.remove(Check.notNull(name));
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		items.putAll(Check.notNull(map));
	}

	/**
	 * Retrieves the value bound to the given key having the given type.
	 *
	 * @param key
	 * @param clazz
	 *            of the returned value
	 * @return value bound to the key
	 *
	 * @throws ResultTypeMismatchException
	 *             if the bound value does not have the specified type.
	 */
	public <T> T get(String key, Class<T> clazz) {
		Object value = get(key);
		if (!clazz.isInstance(value)) {
			throw new ResultTypeMismatchException(value.getClass(), clazz);
		}
		return clazz.cast(value);
	}

	/**
	 * Returns the {@linkplain Integer} bound to the given key.
	 *
	 * @param key
	 * @return value bound to the key
	 * @throws ResultTypeMismatchException
	 *             if the bound value is no {@linkplain Integer}
	 */
	public Integer getInteger(String key) {
		return get(key, Integer.class);
	}

	/**
	 * Returns the {@linkplain Double} bound to the given key.
	 *
	 * @param key
	 * @return value bound to the key
	 * @throws ResultTypeMismatchException
	 *             if the bound value is no {@linkplain Double}
	 */
	public Double getDouble(String key) {
		return get(key, Double.class);
	}

	/**
	 * Returns the {@linkplain Boolean} bound to the given key.
	 *
	 * @param key
	 * @return value bound to the key
	 * @throws ResultTypeMismatchException
	 *             if the bound value is no {@linkplain Boolean}
	 */
	public Boolean getBoolean(String key) {
		return get(key, Boolean.class);
	}

	/**
	 * Returns the {@linkplain String} bound to the given key.
	 *
	 * @param key
	 * @return value bound to the key
	 * @throws ResultTypeMismatchException
	 *             if the bound value is no {@linkplain String}
	 */
	public String getString(String key) {
		return get(key, String.class);
	}

	/**
	 * Returns the {@linkplain Iterable} bound to the given key.
	 * <p>
	 * The generic type parameter of the {@linkplain Iterable} is being inferred depending on the expected type. Thus it
	 * may not be correct and this has to be treated carefully to avoid running into a {@linkplain ClassCastException}
	 * later on.
	 *
	 * @param key
	 * @return value bound to the key
	 * @throws ResultTypeMismatchException
	 *             if the bound value is no {@linkplain Iterable}
	 */
	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getIterable(String key) {
		return get(key, Iterable.class);
	}

	@Override
	public boolean equals(Object context) {
		return context instanceof Context && items.equals(((Context) context).items);
	}

	@Override
	public int hashCode() {
		return items.hashCode();
	}

}
