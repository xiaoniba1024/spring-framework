/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Abstract implementation of {@link TransactionAttributeSource} that caches
 * attributes for methods and implements a fallback policy: 1. specific target
 * method; 2. target class; 3. declaring method; 4. declaring class/interface.
 *
 * <p>Defaults to using the target class's transaction attribute if none is
 * associated with the target method. Any transaction attribute associated with
 * the target method completely overrides a class transaction attribute.
 * If none found on the target class, the interface that the invoked method
 * has been called through (in case of a JDK proxy) will be checked.
 *
 * <p>This implementation caches attributes by method after they are first used.
 * If it is ever desirable to allow dynamic changing of transaction attributes
 * (which is very unlikely), caching could be made configurable. Caching is
 * desirable because of the cost of evaluating rollback rules.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

	/**
	 * Canonical value held in cache to indicate no transaction attribute was
	 * found for this method, and we don't need to look again.
	 */
	@SuppressWarnings("serial")
	// 针对没有事务注解属性的方法进行事务注解属性缓存时使用的特殊值，用于标记该方法没有事务注解属性，
	// 从而不用在首次缓存在信息后，不用再次重复执行真正的分析
	private static final TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute() {
		@Override
		public String toString() {
			return "null";
		}
	};


	/**
	 * Logger available to subclasses.
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Cache of TransactionAttributes, keyed by method on a specific target class.
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	// 方法上的事务注解属性缓存，key使用目标类上的方法，使用类型MethodClassKey来表示
	private final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);


	/**
	 * Determine the transaction attribute for this method invocation.
	 * <p>Defaults to the class's transaction attribute if no method attribute is found.
	 * @param method the method for the current invocation (never {@code null})
	 * @param targetClass the target class for this invocation (may be {@code null})
	 * @return a TransactionAttribute for this method, or {@code null} if the method
	 * is not transactional
	 */
	// 获取指定方法上的注解事务属性
	// Defaults to the class's transaction attribute if no method attribute is found.
	// 如果方法上没有注解事务属性，则使用目标方法所属类上的注解事务属性
	@Override
	@Nullable
	public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		if (method.getDeclaringClass() == Object.class) {
			// 如果目标方法是内置类Object上的方法，总是返回null，这些方法上不应用事务
			return null;
		}

		// First, see if we have a cached value.
		// 先查看针对该方法是否已经获取过其注解事务属性并且已经缓存
		Object cacheKey = getCacheKey(method, targetClass);
		TransactionAttribute cached = this.attributeCache.get(cacheKey);
		if (cached != null) {
			// Value will either be canonical value indicating there is no transaction attribute,
			// or an actual transaction attribute.
			// 目标方法上的事务注解属性信息已经缓存的情况
			if (cached == NULL_TRANSACTION_ATTRIBUTE) {
				// 目标方法上上并没有事务注解属性，但是已经被尝试分析过并且已经被缓存，
				// 使用的值是 NULL_TRANSACTION_ATTRIBUTE,所以这里再次尝试获取其注解事务属性时，
				// 直接返回 null
				return null;
			}
			else {
				return cached;
			}
		}
		else {
			// We need to work it out.
			// 目标方法上的注解事务属性尚未分析过，现在分析获取之
			TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
			// Put it in the cache.
			if (txAttr == null) {
				// 如果目标方法上并没有使用注解事务属性，也缓存该信息，只不过使用的值是一个特殊值:
				// NULL_TRANSACTION_ATTRIBUTE , 此特殊值表明目标方法上没有使用注解事务属性
				this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
			}
			else {
				// 目标方法上使用了注解事务属性，将其放到缓存
				String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
				if (txAttr instanceof DefaultTransactionAttribute) {
					((DefaultTransactionAttribute) txAttr).setDescriptor(methodIdentification);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Adding transactional method '" + methodIdentification + "' with attribute: " + txAttr);
				}
				this.attributeCache.put(cacheKey, txAttr);
			}
			return txAttr;
		}
	}

	/**
	 * Determine a cache key for the given method and target class.
	 * <p>Must not produce same key for overloaded methods.
	 * Must produce same key for different instances of the same method.
	 * @param method the method (never {@code null})
	 * @param targetClass the target class (may be {@code null})
	 * @return the cache key (never {@code null})
	 */
	protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
		return new MethodClassKey(method, targetClass);
	}

	/**
	 * Same signature as {@link #getTransactionAttribute}, but doesn't cache the result.
	 * {@link #getTransactionAttribute} is effectively a caching decorator for this method.
	 * <p>As of 4.1.8, this method can be overridden.
	 * @since 4.1.8
	 * @see #getTransactionAttribute
	 */
	// 查找目标方法上的事务注解属性，但只是查找和返回，并不做缓存,效果上讲，可以认为
	// #getTransactionAttribute 是增加了缓存机制的方法#computeTransactionAttribute
	@Nullable
	protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		// Don't allow no-public methods as required.
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			// 如果事务注解属性分析仅仅针对public方法，而当前方法不是public，则直接返回null
			return null;
		}

		// The method may be on an interface, but we need attributes from the target class.
		// If the target class is null, the method will be unchanged.
		// 参数 method 可能是基于接口的方法，该接口和参数targetClass所对应的类不同(也就是说:
		// targetClass是相应接口的某个实现类),而我们这里需要的属性是要来自targetClass的,
		// 所以这里先获取targetClass上的那个和method对应的方法,这里 method, specificMethod
		// 都可以认为是潜在的目标方法
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// First try is the method in the target class.
		// 首先尝试检查事务注解属性直接标记在目标方法 specificMethod 上
		TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
		if (txAttr != null) {
			// 事务注解属性直接标记在目标方法上
			return txAttr;
		}

		// Second try is the transaction attribute on the target class.
		// 然后尝试检查事务注解属性是否标记在目标方法 specificMethod 所属类上
		txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
		if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
			// 事务注解属性是否标记在目标方法所属类上
			return txAttr;
		}
		// 逻辑走到这里说明目标方法specificMethod，也就是实现类上的目标方法上没有标记事务注解属性
		if (specificMethod != method) {
			// Fallback is to look at the original method.
			// 如果 specificMethod 和 method 不同，则说明 specificMethod 是具体实现类
			// 的方法，method 是实现类所实现接口的方法，现在尝试从 method 上获取事务注解属性
			txAttr = findTransactionAttribute(method);
			if (txAttr != null) {
				return txAttr;
			}
			// Last fallback is the class of the original method.
			// 现在尝试在 method 所属类上查看是否有事务注解属性
			txAttr = findTransactionAttribute(method.getDeclaringClass());
			if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
				return txAttr;
			}
		}
		// specificMethod 方法/所属类上没有事务注解属性，
		// method 方法/所属类上也没有事务注解属性，
		// 所以返回 null
		return null;
	}


	/**
	 * Subclasses need to implement this to return the transaction attribute for the
	 * given class, if any.
	 * @param clazz the class to retrieve the attribute for
	 * @return all transaction attribute associated with this class, or {@code null} if none
	 */
	// 从指定类上获取事务注解属性，由子类负责提供实现
	@Nullable
	protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);

	/**
	 * Subclasses need to implement this to return the transaction attribute for the
	 * given method, if any.
	 * @param method the method to retrieve the attribute for
	 * @return all transaction attribute associated with this method, or {@code null} if none
	 */
	// 从指定方法上获取事务注解属性，由子类负责提供实现
	@Nullable
	protected abstract TransactionAttribute findTransactionAttribute(Method method);

	/**
	 * Should only public methods be allowed to have transactional semantics?
	 * <p>The default implementation returns {@code false}.
	 */
	protected boolean allowPublicMethodsOnly() {
		return false;
	}

}
