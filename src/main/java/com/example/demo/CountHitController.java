package com.example.demo;

import ch.qos.logback.core.util.TimeUtil;
import com.example.demo.impl.CountService;
import com.example.demo.impl.CountServiceExt;
import com.example.demo.impl.SafeCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;


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

/**
 * @note <p>
 * 线程安全性准则1无状态的对象一定是线程安全的
 * Controller的状态变量都不是线程安全。会有并发问题。
 * 而依赖容器注入的service并不会出现线程问题。
 * 前提是service也是个线程安全的类。一般的servie不含有状态变量。
 * 如果还有状态变量。很可能不安全。
 * <p>
 * @see com.example.demo.impl.CountService
 * 这个service就不是线程安全的。
 */
@Controller
@NotThreadSafe
public class CountHitController {
    //atomic包下的操作，保证了操作的原子性，以及线程安全。
    private AtomicInteger safeHits = new AtomicInteger();
    private int unsafehit = 0;

    @Autowired
    @NotThreadSafe
    private CountService cs;

    @ThreadSafe
    @Autowired
    private CountServiceExt countServiceExt;

    @ThreadSafe
    @Autowired
    private SafeCountService safeCountService;


    @RequestMapping("/syncCount")
    @ResponseBody
    @ThreadSafe
    public void getSyncCount(@RequestParam(name = "id") Integer id) {
        long begin = System.currentTimeMillis();
        countServiceExt.bigNumMinusLastNumShouldEqualsOne(id);
        long end = System.currentTimeMillis();
        System.out.println(getCunrrentTime()+"     "+(end - begin));
       // countServiceExt.doUnSafeService(id);

    }

    private static String getCunrrentTime(){
        DateFormat df = new SimpleDateFormat("yy-MM-dd hh:mm:ss-SSS");
        Date d = new Date();

        return df.format(d).toString();


    }
    @RequestMapping("/FastSyncCount")
    @ResponseBody
    @ThreadSafe
    public void getFastSyncCount(@RequestParam(name = "id") Integer id) {
        long begin = System.currentTimeMillis();
        countServiceExt.FastSafeService(id);
        long end = System.currentTimeMillis();
        System.out.println(getCunrrentTime()+"      "+ + (end - begin));
        // countServiceExt.doUnSafeService(id);

    }

    @RequestMapping("/UnSafeService")
    @ResponseBody
    @NotThreadSafe
    public void getSyncCount2(@RequestParam(name = "id") Integer id) {
        long begin = System.currentTimeMillis();
        countServiceExt.doUnSafeService(id);
        long end = System.currentTimeMillis();
        System.out.println(getCunrrentTime()+"      "+ + (end - begin));

    }

    @RequestMapping("/safeCount")
    @ResponseBody
    @ThreadSafe
    public void getSafeCount() {
        //countServiceExt.SafeButBadService(a);
        safeCountService.getLongHits();

    }


    @RequestMapping("/hitme")
    @ResponseBody
    @ThreadSafe
    public void hitCount(HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        int serviceCount = cs.count();
        safeHits.incrementAndGet();

//       加锁使unsafehit变为原子性操作。这样就不会存在线程问题
        /*synchronized (this)*/
        synchronized (CountHitController.class) {
            unsafehit++;
        }

        String currentTheadName = Thread.currentThread().getName();
        try {


            response.getWriter().print("当前运行线程为" +
                    currentTheadName +
                    "并发实际访问次数"
                    + safeHits
                    + System.lineSeparator() + "加synchronize锁记录次数为" + unsafehit
                    + System.lineSeparator() + "CountService计算的次数为=" + serviceCount


            );

            System.out.println("并发实际访问次数="
                    + safeHits
                    + System.lineSeparator() + "并发加synchronize锁访问次数=" + unsafehit
                    + System.lineSeparator() + "CountService计算的次数为=" + serviceCount

            );

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.getOutputStream().print(e.getMessage().toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

    @RequestMapping("/unsafe")
    @ResponseBody
    @NotThreadSafe
    public void unSafeHitCount(HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");

        /*
        //       加锁使unsafehit变为原子性操作。这样就不会存在线程问题
                synchronized (this) {
                    unsafehit++;
                }
        */
        String currentTheadName = Thread.currentThread().getName();

        try {
            safeHits.incrementAndGet();
            unsafehit++;
            response.getWriter().print("当前运行线程为" +
                    currentTheadName +
                    "并发实际访问次数"
                    + safeHits
                    + System.lineSeparator() + "非线程访问记录次数为" + unsafehit
            );

            System.out.println("并发实际访问次数="
                    + safeHits
                    + System.lineSeparator() + "并发非原子性下访问次数=" + unsafehit);

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.getOutputStream().print(e.getMessage().toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }


}

