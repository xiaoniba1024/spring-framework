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
package org.geekbang.thinking.in.spring.ioc.overview.container;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * ע������ {@link ApplicationContext} ��Ϊ IoC ����ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
@Configuration
public class AnnotationApplicationContextAsIoCContainerDemo {

    public static void main(String[] args) {
        // ���� BeanFactory ����
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // ����ǰ�� AnnotationApplicationContextAsIoCContainerDemo ��Ϊ�����ࣨConfiguration Class��
        applicationContext.register(AnnotationApplicationContextAsIoCContainerDemo.class);
        // ����Ӧ��������
        applicationContext.refresh();
        // �������Ҽ��϶���
        lookupCollectionByType(applicationContext);

        // �ر�Ӧ��������
        applicationContext.close();

    }

    /**
     * ͨ�� Java ע��ķ�ʽ��������һ�� Bean
     */
    @Bean
    public User user() {
        User user = new User();
        user.setId(1L);
        user.setName("С���");
        return user;
    }

    private static void lookupCollectionByType(BeanFactory beanFactory) {
        if (beanFactory instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
            Map<String, User> users = listableBeanFactory.getBeansOfType(User.class);
            System.out.println("���ҵ������е� User ���϶���" + users);
        }
    }

}
