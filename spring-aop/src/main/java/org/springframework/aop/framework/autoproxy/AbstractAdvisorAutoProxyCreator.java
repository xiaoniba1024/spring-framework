/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.aop.framework.autoproxy;

import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 *
 * <p>Subclasses may override the {@link #findCandidateAdvisors()} method to
 * return a custom list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 *
 * <p>Advisors or advices requiring ordering should implement the
 * {@link org.springframework.core.Ordered} interface. This class sorts
 * Advisors by Ordered order value. Advisors that don't implement the
 * Ordered interface will be considered as unordered; they will appear
 * at the end of the advisor chain in undefined order.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {
	// 这个类是重点
	@Nullable
	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;

	// 覆盖基类的setBeanFactory方法
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		// 首先调用基类的setBeanFactory方法
		super.setBeanFactory(beanFactory);
		// 要求所设置的 beanFactory 也就是Spring bean容器必须使用类型 ConfigurableListableBeanFactory,
		// 否则抛出异常，声明当前 AdvisorAutoProxyCreator 只针对类型为 ConfigurableListableBeanFactory的容器工作
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		// 上面设置完beanFactory检查过类型之后，立即初始化 beanFactory，初始化逻辑 :
		// 准备一个针对该beanFactory的BeanFactoryAdvisorRetrievalHelperAdapter记录到
		// this.advisorRetrievalHelper, 用于从beanFactory获取Spring Advisor。
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}
	// 初始化 beanFactory，初始化逻辑 :
	// 准备一个针对该beanFactory的BeanFactoryAdvisorRetrievalHelperAdapter记录到
	// this.advisorRetrievalHelper, 用于从beanFactory获取Spring Advisor。
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}

	// 找到针对某个bean的所有符合条件的Advisor/Advice,如果结果为null，将不会为该bean创建代理
	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
		// findEligibleAdvisors：显然这个是具体的实现方法了。
		// eligible：合格的  合适的
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			// DO_NOT_PROXY 其实是一个为 null 的常量，用于表示：
			// 如果找不到符合条件的Advisor,就不要为该bean创建代理
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * @param beanClass the clazz to find advisors for
	 * @param beanName the name of the currently proxied bean
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 使用 findCandidateAdvisors() 找到容器中所有的 Advisor :
		// 可以参考当前类 findCandidateAdvisors() 的实现 : 仅获取容器中所有Spring Advisors
		// 或者参考 AnnotationAwareAspectJAutoProxyCreator 的实现 :
		// 获取容器中所有Spring Advisors + 所有封装自每个AspectJ切面类中的每个advice方法的Advisors
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 过滤所有找到的Advisor,看看它们对参数bean是否需要应用
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// 对以上需要应用的advisor list作扩展，具体如何扩展，参考当前类或者子类对extendAdvisors()的具体实现
		// 当前类 extendAdvisors(): 什么都不做，只是个空方法
		// AnnotationAwareAspectJAutoProxyCreator#extendAdvisors():增加一个ExposeInvocationInterceptor
		extendAdvisors(eligibleAdvisors);
		// 如果最终还有，那就排序吧
		if (!eligibleAdvisors.isEmpty()) {
			// 对适用的advisor做排序，具体排序方法参考sortAdvisors(),子类也可以定制该排序逻辑
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	// 该方法使用advisorRetrievalHelper找到容器中所有的Spring Advisor beans用于自动代理，
	// 子类可以覆盖或者扩展该方法。
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * Search the given candidate Advisors to find all Advisors that
	 * can apply to the specified bean.
	 * @param candidateAdvisors the candidate Advisors
	 * @param beanClass the target's bean class
	 * @param beanName the target's bean name
	 * @return the List of applicable Advisors
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {
		// ProxyCreationContext 使用了一个 ThreadLocal 变量保持当前正在进行代理创建的bean，
		// 在代理创建过程中，比如对pointcut表达式求值时会使用到 ProxyCreationContext,
		// 由此可见，某个bean的代理创建必须在同一个线程内完成，不能跨线程
		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			// 具体检查每个advisor是否需要应用到该bean的逻辑委托给AopUtils完成，这里不做深入分析,
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			// 已经完成advisor和bean的匹配过程，清除ProxyCreationContext
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * @param beanName the name of the Advisor bean
	 * @return whether the bean is eligible
	 */
	//  返回某个bean是否需要被代理，这里是缺省实现，总是返回true，子类可以重写该方法实现自己的定制逻辑
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * @param advisors the source List of Advisors
	 * @return the sorted List of Advisors
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * Extension hook that subclasses can override to register additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * <p>The default implementation is empty.
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * @param candidateAdvisors the Advisors that have already been identified as
	 * applying to a given bean
	 */
	// 提供给子类一个扩展candidateAdvisors的机会，至于如何扩展，看子类的目的
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * This auto-proxy creator always returns pre-filtered Advisors.
	 */
	// 此处复写了父类的方法，返回true了，表示
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
	 */
	// 自定义的一个BeanFactoryAdvisorRetrievalHelper，用于从容器中获取所有的 Spring Advisor
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
