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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * ע�� BeanDefinition ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
// 3. ͨ�� @Import �����е���
@Import(AnnotationBeanDefinitionDemo.Config.class)
public class AnnotationBeanDefinitionDemo {

    public static void main(String[] args) {
        // ���� BeanFactory ����
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // ע�� Configuration Class�������ࣩ
        applicationContext.register(AnnotationBeanDefinitionDemo.class);

        // ͨ�� BeanDefinition ע�� API ʵ��
        // 1.���� Bean ��ע�᷽ʽ
        registerUserBeanDefinition(applicationContext, "mercyblitz-user");
        // 2. ������ Bean ��ע�᷽��
        registerUserBeanDefinition(applicationContext);

        // ���� Spring Ӧ��������
        applicationContext.refresh();
        // ����������������
        System.out.println("Config ���͵����� Beans" + applicationContext.getBeansOfType(Config.class));
        System.out.println("User ���͵����� Beans" + applicationContext.getBeansOfType(User.class));
        // ��ʾ�عر� Spring Ӧ��������
        applicationContext.close();
    }

    public static void registerUserBeanDefinition(BeanDefinitionRegistry registry, String beanName) {
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(User.class);
        beanDefinitionBuilder
                .addPropertyValue("id", 1L)
                .addPropertyValue("name", "С���");

        // �ж���� beanName ��������ʱ
        if (StringUtils.hasText(beanName)) {
            // ע�� BeanDefinition
            registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        } else {
            // ������ Bean ע�᷽��
            BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinitionBuilder.getBeanDefinition(), registry);
        }
    }

    public static void registerUserBeanDefinition(BeanDefinitionRegistry registry) {
        registerUserBeanDefinition(registry, null);
    }

    // 2. ͨ�� @Component ��ʽ
    @Component // ���嵱ǰ����Ϊ Spring Bean�������
    public static class Config {

        // 1. ͨ�� @Bean ��ʽ����

        /**
         * ͨ�� Java ע��ķ�ʽ��������һ�� Bean
         */
        @Bean(name = {"user", "xiaomage-user"})
        public User user() {
            User user = new User();
            user.setId(1L);
            user.setName("С���");
            return user;
        }
    }


}
