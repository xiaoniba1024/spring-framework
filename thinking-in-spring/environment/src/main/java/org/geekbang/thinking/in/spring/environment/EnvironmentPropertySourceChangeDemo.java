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
package org.geekbang.thinking.in.spring.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Environment} ��������Դ���ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Environment
 * @since
 */
public class EnvironmentPropertySourceChangeDemo {

    @Value("${user.name}")  // ���߱���̬��������
    private String userName;

    // PropertySource("first-property-source") { user.name = С���}
    // PropertySource( Java System Properties) { user.name = mercyblitz }

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // ע�� Configuration Class
        context.register(EnvironmentPropertySourceChangeDemo.class);

        // �� Spring Ӧ������������ǰ������ Environment �е� PropertySource
        ConfigurableEnvironment environment = context.getEnvironment();
        // ��ȡ MutablePropertySources ����
        MutablePropertySources propertySources = environment.getPropertySources();
        // ��̬�ز��� PropertySource �� PropertySources ��
        Map<String, Object> source = new HashMap<>();
        source.put("user.name", "С���");
        MapPropertySource propertySource = new MapPropertySource("first-property-source", source);
        propertySources.addFirst(propertySource);

        // ���� Spring Ӧ��������
        context.refresh();

        source.put("user.name", "007");

        EnvironmentPropertySourceChangeDemo environmentPropertySourceChangeDemo = context.getBean(EnvironmentPropertySourceChangeDemo.class);

        System.out.println(environmentPropertySourceChangeDemo.userName);

        for (PropertySource ps : propertySources) {
            System.out.printf("PropertySource(name=%s) 'user.name' ���ԣ�%s\n", ps.getName(), ps.getProperty("user.name"));
        }

        // �ر� Spring Ӧ��������
        context.close();
    }
}
