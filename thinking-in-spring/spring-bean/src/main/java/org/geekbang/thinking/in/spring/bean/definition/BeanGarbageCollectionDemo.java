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

import org.geekbang.thinking.in.spring.bean.factory.UserFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Bean �������գ�GC��ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class BeanGarbageCollectionDemo {

    public static void main(String[] args) throws InterruptedException {
        // ���� BeanFactory ����
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // ע�� Configuration Class�������ࣩ
        applicationContext.register(BeanInitializationDemo.class);
        // ���� Spring Ӧ��������
        applicationContext.refresh();
        // �ر� Spring Ӧ��������
        applicationContext.close();
        Thread.sleep(5000L);
        // ǿ�ƴ��� GC
        System.gc();
        Thread.sleep(5000L);
    }

}
