/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.appdynamics.monitors.mongo.json.server;

public class GlobalLock {
    private ActiveClients activeClients;
    private CurrentQueue currentQueue;
    private Number lockTime;
    private Number totalTime;

    public ActiveClients getActiveClients() {
        return this.activeClients;
    }

    public void setActiveClients(ActiveClients activeClients) {
        this.activeClients = activeClients;
    }

    public CurrentQueue getCurrentQueue() {
        return this.currentQueue;
    }

    public void setCurrentQueue(CurrentQueue currentQueue) {
        this.currentQueue = currentQueue;
    }

    public Number getLockTime() {
        return this.lockTime;
    }

    public void setLockTime(Number lockTime) {
        this.lockTime = lockTime;
    }

    public Number getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime(Number totalTime) {
        this.totalTime = totalTime;
    }
}
