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
package org.geekbang.thinking.in.spring.application.context.lifecycle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.LiveBeansView;

import java.io.IOException;

import static org.springframework.context.support.LiveBeansView.MBEAN_DOMAIN_PROPERTY_NAME;

/**
 * {@link LiveBeansView} ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LiveBeansView
 * @since
 */
public class LiveBeansViewDemo {

    public static void main(String[] args) throws IOException {

        // ��� LiveBeansView �� ObjectName �� domain
        System.setProperty(MBEAN_DOMAIN_PROPERTY_NAME, "org.geekbang.thinking.in.spring");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // ע�� Configuration Class
        context.register(LiveBeansViewDemo.class);

        // ���� Spring Ӧ��������
        context.refresh();

        System.out.println("�����������...");
        System.in.read();

        context.close();
        // �ر� Spring Ӧ��������
    }

    /**
     * [ { "context": "org.springframework.context.annotation.AnnotationConfigApplicationContext@2f7a2457", "parent": null, "beans": [ { "bean": "liveBeansViewDemo", "aliases": [], "scope": "singleton", "type": "org.geekbang.thinking.in.spring.application.context.lifecycle.LiveBeansViewDemo", "resource": "null", "dependencies": [] }] }]
     */
}
