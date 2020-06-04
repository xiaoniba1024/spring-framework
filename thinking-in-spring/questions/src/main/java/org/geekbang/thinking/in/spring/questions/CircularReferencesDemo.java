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

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * BeanFactory ѭ�����ã�������ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class CircularReferencesDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // ע�� Configuration Class
        context.register(CircularReferencesDemo.class);

        // �������Ϊ false�����׳��쳣��Ϣ�磺currently in creation: Is there an unresolvable circular reference?
        // Ĭ��ֵΪ true
        context.setAllowCircularReferences(true);

        // ���� Spring Ӧ��������
        context.refresh();

        System.out.println("Student : " + context.getBean(Student.class));
        System.out.println("ClassRoom : " + context.getBean(ClassRoom.class));

        // �ر� Spring Ӧ��������
        context.close();
    }

    @Bean
    public static Student student() {
        Student student = new Student();
        student.setId(1L);
        student.setName("����");
        return student;
    }

    @Bean
    public static ClassRoom classRoom() {
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName("C122");
        return classRoom;
    }
}
