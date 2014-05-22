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

	// TODO #18 rebind and containsKey
	// TODO #18 require map to contain the key when calling get

	private final Map<Object, Object> items;

	public Context() {
		items = Maps.newHashMap();
	}

	public Context(Context context) {
		this();
		items.putAll(Check.notNull(context).items);
	}

	/**
	 * Binds the given value to the given key.
	 *
	 * @param key
	 * @param value
	 * @throws KeyAlreadyBoundException
	 *             if there is already a value bound to the key
	 */
	public void bind(Object key, @Nullable Object value) {
		Check.notNull(key);
		if (items.containsKey(key)) {
			throw new KeyAlreadyBoundException(key);
		}

		items.put(key, value);
	}

	/**
	 * Retrieves the value bound to the given key.
	 *
	 * @param key
	 * @return value bound to that key
	 */
	public Object get(Object key) {
		return items.get(Check.notNull(key));
	}

	/**
	 * Unbinds the value bound to the specified key.
	 *
	 * @param key
	 * @throws KeyNotBoundException
	 *             if there is no value bound to that key
	 */
	public void unbind(Object key) {
		Check.notNull(key);
		if (items.remove(key) == null) {
			throw new KeyNotBoundException(key);
		}
	}

	/**
	 * Binds all values of the given map to their given key.
	 *
	 * @param map
	 * @throws KeyAlreadyBoundException
	 *             if there is at least one of the given keys has already bound values
	 */
	public void bindAll(Map<? extends Object, ? extends Object> map) {
		Check.notNull(map);
		for (Object key : map.keySet()) {
			bind(key, map.get(key));
		}
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
	public <T> T get(Object key, Class<T> clazz) {
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
	public Integer getInteger(Object key) {
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
	public Double getDouble(Object key) {
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
	public Boolean getBoolean(Object key) {
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
	public String getString(Object key) {
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
	public <T> Iterable<T> getIterable(Object key) {
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
