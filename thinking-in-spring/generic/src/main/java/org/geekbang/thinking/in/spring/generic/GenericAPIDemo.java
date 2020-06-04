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
package org.geekbang.thinking.in.spring.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Java ���� API ʾ��
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class GenericAPIDemo {

    public static void main(String[] args) {

        // ԭ������ primitive types : int long float
        Class intClass = int.class;

        // �������� array types : int[],Object[]
        Class objectArrayClass = Object[].class;

        // ԭʼ���� raw types : java.lang.String
        Class rawClass = String.class;

        // ���Ͳ������� parameterized type
        ParameterizedType parameterizedType = (ParameterizedType) ArrayList.class.getGenericSuperclass();

        //  parameterizedType.getRawType() = java.util.AbstractList

        // �������ͱ��� Type Variable:

        System.out.println(parameterizedType.toString());

        // <E>
        Type[] typeVariables = parameterizedType.getActualTypeArguments();

        Stream.of(typeVariables)
                .map(TypeVariable.class::cast) // Type -> TypeVariable
                .forEach(System.out::println);

    }
}
