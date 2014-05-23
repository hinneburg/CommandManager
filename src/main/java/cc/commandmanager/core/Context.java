package cc.commandmanager.core;

import java.util.Map;

import javax.annotation.Nullable;

import net.sf.qualitycheck.Check;

import com.google.common.collect.Maps;

/**
 * Execution context of the {@linkplain CommandManagement}. {@linkplain Command}s can use this context to store or read
 * values during their execution. The context will be passed along the execution graph. Values bound to keys of the
 * context must be unique, i.e. a {@linkplain Command} cannot accidently overwrite an existing key value binding.
 */
public class Context {

	private final Map<Object, Object> items;

	/**
	 * Creates a new empty {@linkplain Context}.
	 */
	public Context() {
		items = Maps.newHashMap();
	}

	/**
	 * Creates a new context and immediately binds all values bound to the given context.
	 *
	 * @param context
	 *            whose bound values will also be bound in the new context
	 */
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
	 * Unbinds the value bound to the specified key.
	 *
	 * @param key
	 * @throws KeyNotBoundException
	 *             if there is no value bound to that key
	 */
	public void unbind(Object key) {
		Check.notNull(key);
		checkMapContainsKey(items, key);
		items.remove(key);
	}

	/**
	 * Rebinds a different value to a key with an already bound value. This will give the same result as successive
	 * calls of {@linkplain Context#unbind(Object)} and {@linkplain Context#bind(Object, Object)}.
	 *
	 * @param key
	 * @param value
	 * @throws KeyNotBoundException
	 *             if there is no value bound to the key
	 */
	public void rebind(Object key, @Nullable Object value) {
		unbind(key);
		bind(key, value);
	}

	/**
	 * Binds all values of the given map to their given key.
	 *
	 * @param map
	 * @throws KeyAlreadyBoundException
	 *             if at least one of the given keys has already bound values
	 */
	public void bindAll(Map<? extends Object, ? extends Object> map) {
		Check.notNull(map);
		for (Object key : map.keySet()) {
			bind(key, map.get(key));
		}
	}

	/**
	 * Checks whether the given key has some value bound to in the context.
	 *
	 * @param key
	 * @return if a value is bound to the key
	 */
	public boolean containsKey(Object key) {
		return items.containsKey(Check.notNull(key));
	}

	/**
	 * Retrieves the value bound to the given key.
	 *
	 * @param key
	 * @return value bound to that key
	 */
	public Object get(Object key) {
		Check.notNull(key);
		checkMapContainsKey(items, key);
		return items.get(key);
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

	private static void checkMapContainsKey(Map<Object, Object> map, Object key) {
		if (!map.containsKey(key)) {
			throw new KeyNotBoundException(key);
		}
	}

}
