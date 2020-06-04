/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geekbang.thinking.in.spring.bean.definition;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * {@link org.springframework.beans.factory.config.BeanDefinition} ����ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class BeanDefinitionCreationDemo {

    public static void main(String[] args) {

        // 1.ͨ�� BeanDefinitionBuilder ����
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
        // ͨ����������
        beanDefinitionBuilder
                .addPropertyValue("id", 1)
                .addPropertyValue("name", "С���");
        // ��ȡ BeanDefinition ʵ��
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        // BeanDefinition ���� Bean ��̬�������Զ����޸�

        // 2. ͨ�� AbstractBeanDefinition �Լ�������
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        // ���� Bean ����
        genericBeanDefinition.setBeanClass(User.class);
        // ͨ�� MutablePropertyValues ������������
        MutablePropertyValues propertyValues = new MutablePropertyValues();
//        propertyValues.addPropertyValue("id", 1);
//        propertyValues.addPropertyValue("name", "С���");
        propertyValues
                .add("id", 1)
                .add("name", "С���");
        // ͨ�� set MutablePropertyValues ������������
        genericBeanDefinition.setPropertyValues(propertyValues);
    }
}
