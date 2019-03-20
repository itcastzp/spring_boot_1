package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Controller
@NotThreadSafe
public class LazyLodeController {
    static {
        System.out.println("我应该只被初始化一次");

    }

    static void get() {
        System.out.println("不调用我就不被初始化");
    }

    private volatile Object instance = null;


    /**
     * 内部静态类延迟加载机制。
     * 当外部类调用内部类的时候，内部类的才会去初始化。
     */
    private static class LazyHolder {
        static {
            System.out.println("我是内部类-------有人调用我了我就被初始化，没人调用我我就安静的待着！！！");

        }
        private static Object instance = new Object();
    }

    @RequestMapping("/SingleInstance")
    @ResponseBody
    @NotThreadSafe
    public void responseInstance1(HttpServletResponse response) {

        //竞态条件
        // 此时的instance是否为null，取决于不可预测的时序，线程的调度方式，
        // 以及初始化对象instance的时间。这样单例模式就是失败的，会返回不同的
        // 对象。
        //warning:此处让线程等待一会，则在大量并发下就出现了返回多个对象的结果。并发测试使用jmeter模拟。
        long begintime = System.currentTimeMillis();
        if (instance == null) {

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = new Object();
        }
        long endtime = System.currentTimeMillis();
        //有一个现象，每次应用刚启动时第一次出现并发时会创建很多不同对象，出现线程问题，但是以后就不会再创建新的对象了。不知道什么原因。
        // 且以后每个线程使用的实例对象都是第一次大量并发线程初始化时最后一个线程创建的实例。

        System.out.println("创建对象" + instance + "用时---------" + (endtime - begintime) / 1000);
        try {
            response.getWriter().print(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping("/SafeSingleInstance")
    @ResponseBody
    @ThreadSafe
    public void responseInstance(HttpServletResponse response) {

        //此时的instance是否为null，取决于不可预测的时序，线程的调度方式，
        // 以及初始化对象instance的时间。这样单例模式就是失败的，会返回不同的
        // 对象。
        if (instance == null) {
            synchronized (LazyLodeController.class) {
                if (instance == null) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    instance = new Object();
                }
            }
        }
        System.out.println(instance);
        try {
            response.getWriter().print(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @ResponseBody
    @RequestMapping("/getSingle")
    @ThreadSafe
    public void responseInnerStaticClassInstance() {
        //内部类延时加载除了线程安全外，还有节省资源的好处。
        // 不同于饿汉模式，不管需不需要都要加载。
        // 假设创建的对象开销很大，当有人需要时，内部类再去初始化对象，没人用就不加载节省开支，提高性能。
        instance = LazyHolder.instance;
        System.out.println("通过内部静态类懒加载的实例" + instance);
    }

}
