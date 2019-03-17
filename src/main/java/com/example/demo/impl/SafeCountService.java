package com.example.demo.impl;

import com.example.demo.ThreadSafe;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

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
@Service
@ThreadSafe
public class SafeCountService {
    //当在无状态的类中添加了状态时（添加成员变量）如果该状态完全由线程安全的对象管理
    // 那么这个类仍然是线程安全的。但是如果从一个变为多个。那就更不简单。
    //@See CountServieExt
    @ThreadSafe
    private AtomicLong longHits = new AtomicLong();


    @ThreadSafe
    public long getLongHits() {
        return longHits.incrementAndGet();
    }

}
