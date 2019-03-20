package com.example.demo.impl;

import com.example.demo.NotThreadSafe;
import com.example.demo.ThreadSafe;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
@NotThreadSafe
@Service
public class CountServiceExt {
    /**
     * 加入更多的线程安全的状态变量，并不代表着此类是线程安全的。
     */
    private AtomicInteger lastnumber = new AtomicInteger();
    private AtomicInteger firstnumber = new AtomicInteger();
    private AtomicReference<Integer> big = new AtomicReference<>();


    /**
     * 线程安全性定义，存在竞态条件就不是安全的，要想安全，就要保证不变形条件不被破坏
     * 而此类的不变形条件之一是：
     * 只有确保big中的值比lastnumber值多1才能保证线程安全
     * 当不想变性条件涉及多个变量，则操作这些变量时需要是原子操作。
     * 即更新一个变量，也要同时更新其他变量。
     * <p>
     * --------要保证状态的一致性，需要在单个原子操作中更新到所有相关的状态变量------
     * <p>
     * synchronize支持原子性。被他包裹的代码是原子操作。
     */
    @NotThreadSafe
    public void doUnSafeService(Integer i) {
        if (i.equals(lastnumber)) {
            big.get();
        } else {
            lastnumber.set(i);
           /* try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            big.set(i + 1);
        }
        System.out.println("big"+big+"-----lastNumber"+lastnumber);
    }

    /**
     * 线程是安全的但是并发性很糟糕，性能很差。
     *
     * @param i
     */
    @ThreadSafe
    public synchronized void bigNumMinusLastNumShouldEqualsOne(Integer i) {

        if (i.equals(lastnumber)) {
            big.get();
        } else {
            lastnumber.set(i);
            big.set(i + 1);
        }

        System.out.println("big"+big+"-----lastNumber"+lastnumber);
    }

    @ThreadSafe
    public void FastSafeService(Integer i) {
        synchronized (this) {

            if (i.equals(lastnumber)) {
                big.get();
            } else {
                lastnumber.set(i);
                big.set(i + 1);
            }
        }
        System.out.println("big"+big+"-----lastNumber"+lastnumber);

    }


}
