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
package org.geekbang.thinking.in.spring.questions;

import org.geekbang.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * Bean �Ƿ񻺴�ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ObjectFactory
 * @see ObjectProvider
 * @since
 */
public class BeanCachingDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // ע�� Configuration Class
        context.register(BeanCachingDemo.class);

        // ���� Spring Ӧ��������
        context.refresh();

        // BeanCachingDemo �� Configuration Class��Singleton Scope Bean
        BeanCachingDemo beanCachingDemo = context.getBean(BeanCachingDemo.class);

        for (int i = 0; i < 9; i++) {
            // Singleton Scope Bean �Ǵ��ڻ���
            System.out.println(beanCachingDemo == context.getBean(BeanCachingDemo.class));
        }

        User user = context.getBean(User.class);

        for (int i = 0; i < 9; i++) {
            // Prototype Scope Bean
            System.out.println(user == context.getBean(User.class));
        }

        // �ر� Spring Ӧ��������
        context.close();
    }

    @Bean
    @Scope("prototype") // ԭ�� scope
    public static User user() {
        User user = new User();
        user.setId(1L);
        user.setName("С���");
        return user;
    }
}
