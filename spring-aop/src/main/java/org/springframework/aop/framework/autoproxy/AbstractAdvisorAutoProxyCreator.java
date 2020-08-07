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

	// 重写了setBeanFactory方法，事需要保证bean工厂必须是ConfigurableListableBeanFactory
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		// 就这一句话：this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory)
		// 对Helper进行初始化，找advisor最终事委托给他了的
		// BeanFactoryAdvisorRetrievalHelperAdapter继承自BeanFactoryAdvisorRetrievalHelper,为私有内部类，主要重写了isEligibleBean（）方法，调用.this.isEligibleAdvisorBean(beanName)方法
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}

	// 这是复写父类的方法，也是实现代理方式。找到作用在这个Bean里面的切点方法
	// 当然 最终最终事委托给BeanFactoryAdvisorRetrievalHelper去做的
	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
		// findEligibleAdvisors：显然这个是具体的实现方法了。
		// eligible：合格的  合适的
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
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
	// 找出合适的Advisor们~~~  主要分了下面几步
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 首先找出所有的候选的Advisors，（根据名字判断）实现见下面~~~~
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 对上面找到的候选的Advisors们，进行过滤操作~~~  看看Advisor能否被用在Bean上（根据Advisor的PointCut判断）
		// 主要依赖于AopUtils.findAdvisorsThatCanApply()方法  在工具类讲解中有详细分析的
		// 逻辑简单概述为：看目标类是不是符合代理对象的条件，如果符合就把Advisor加到集合中，最后返回集合
		// 简单的说：它就是会根据ClassFilter和MethodMatcher等等各种匹配。（但凡只有有一个方法被匹配上了，就会给他创建代理类了）
		// 方法用的ReflectionUtils.getAllDeclaredMethods，**因此哪怕是私有方法，匹配上都会给创建的代理对象，这点务必要特别特别的注意**
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// 提供一个钩子。子类可以复写此方法  然后对eligibleAdvisors进行处理（增加/删除/修改等等）
		// AspectJAwareAdvisorAutoProxyCreator提供了实现
		extendAdvisors(eligibleAdvisors);
		// 如果最终还有，那就排序吧
		if (!eligibleAdvisors.isEmpty()) {
			// 默认排序方式：AnnotationAwareOrderComparator.sort()排序  这个排序和Order接口有关~~~
			// 但是子类：AspectJAwareAdvisorAutoProxyCreator有复写此排序方法，需要特别注意~~~
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	// 找到候选的Advisor们~~~~   抽象类自己的实现，是直接把这件事委托给了advisorRetrievalHelper
	// AnnotationAwareAspectJAutoProxyCreator对它有复写
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

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * @param beanName the name of the Advisor bean
	 * @return whether the bean is eligible
	 */
	// 判断给定的BeanName这个Bean，是否是合格的(BeanFactoryAdvisorRetrievalHelper里会用到这个属性)
	// 其中：DefaultAdvisorAutoProxyCreator和InfrastructureAdvisorAutoProxyCreator有复写
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
