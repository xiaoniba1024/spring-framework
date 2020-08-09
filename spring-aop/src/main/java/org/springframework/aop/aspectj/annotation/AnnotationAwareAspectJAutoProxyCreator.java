/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} subclass that processes all AspectJ
 * annotation aspects in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their
 * advice applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by
 * an include pattern will be considered as defining aspects to use for Spring auto-proxying.
 *
 * <p>Processing of Spring Advisors follows the rules established in
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	@Nullable
	private List<Pattern> includePatterns;
	// 唯一实现类：ReflectiveAspectJAdvisorFactory
	// 作用：基于@Aspect时,创建Spring AOP的Advice
	// 里面会对标注这些注解Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class的方法进行排序
	// 然后把他们都变成Advisor( getAdvisors()方法 )
	@Nullable
	private AspectJAdvisorFactory aspectJAdvisorFactory;
	// 该工具类用来从bean容器，也就是BeanFactory中获取所有使用了@AspectJ注解的bean
	// 就是这个方法：aspectJAdvisorsBuilder.buildAspectJAdvisors()
	@Nullable
	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
	 * <p>Default is to consider all @AspectJ beans as eligible.
	 */
	// 很显然，它还支持我们自定义一个正则的模版
	// isEligibleAspectBean()该方法使用此模版，从而决定使用哪些Advisor
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}
	// 可以自己实现一个AspectJAdvisorFactory  否则用默认的ReflectiveAspectJAdvisorFactory
	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}
	// 此处一定要记得调用：super.initBeanFactory(beanFactory);
	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 使用基类方法初始化 beanFactory，真正的逻辑时构造一个能够从容器中获取所有
		// Spring Advisor bean的BeanFactoryAdvisorRetrievalHelper
		super.initBeanFactory(beanFactory);
		// 下面的逻辑是构造 aspectJAdvisorsBuilder , 用于通过反射方法从容器中获取
		// 所有带有@AspectJ注解的 beans
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}

	// 找到容器中所有的Spring Advisor beans 和 @AspectJ 注解的 beans，都封装成
	// Advisor 形式返回一个列表
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		// 使用基类Spring Advisor查找机制查找容器中所有的Spring Advisor
		// 其实就是父类使用所构造的BeanFactoryAdvisorRetrievalHelper从
		// 容器中获取所有的Spring Advisor beans。
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 使用aspectJAdvisorsBuilder从容器中获取所有@AspectJ 注解的bean，并将它们包装成Advisor
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		// 现在列表advisors包含容器中所有的Spring Advisor beans 和 @AspectJ 注解的 beans,
		// 现在它们都以 Advisor 的形式存在
		return advisors;
	}
	// 判断指定的类是否是一个基础设置类:
	// 1. 如果父类的 isInfrastructureClass 断定它是一个基础设施类，就认为它是;
	// 2. 如果这是一个@Aspect注解的类，也认为这是一个基础设施类;
	// 一旦某个bean类被认为是基础设施类，那么将不会对该类实施代理机制
	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// Previously we setProxyTargetClass(true) in the constructor, but that has too
		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * Check whether the given aspect bean is eligible for auto-proxying.
	 * <p>If no &lt;aop:include&gt; elements were used then "includePatterns" will be
	 * {@code null} and all beans are included. If "includePatterns" is non-null,
	 * then one of the patterns must match.
	 */
	// 检查给定名称的bean是否是符合条件的Aspect bean。这里是依据属性 includePatterns 做检查的，
	// 当 includePatterns 属性为 null 时，总是返回 true。
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(
				ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}
