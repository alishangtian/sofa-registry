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
package com.alipay.sofa.registry.server.session.listener;

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.scheduler.task.CancelDataTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: CancelDataTaskListener.java, v 0.1 2017-12-27 12:02 shangyu.wh Exp $
 */
public class CancelDataTaskListener implements TaskListener {

    /**
     * store subscribers
     */
    @Autowired
    private Interests                           sessionInterests;

    /**
     * store publishers
     */
    @Autowired
    private DataStore                           sessionDataStore;

    @Autowired
    private Watchers                            sessionWatchers;

    /**
     * transfer data to DataNode
     */
    @Autowired
    private DataNodeService                     dataNodeService;

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    @Autowired
    private TaskProcessor                       dataNodeSingleTaskProcessor;

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            singleTaskDispatcher = TaskDispatchers.createSingleTaskDispatcher(
                TaskDispatchers.getDispatcherName(TaskType.CANCEL_DATA_TASK.getName()), 10000, 80,
                1000, 100, dataNodeSingleTaskProcessor);
        }
        return singleTaskDispatcher;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.CANCEL_DATA_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {

        SessionTask cancelDataTask = new CancelDataTask(sessionInterests, sessionDataStore,
            sessionWatchers, dataNodeService, sessionServerConfig);

        cancelDataTask.setTaskEvent(event);
        getSingleTaskDispatcher().dispatch(cancelDataTask.getTaskId(), cancelDataTask,
            cancelDataTask.getExpiryTime());
    }
}