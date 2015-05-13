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

public class Cursors {
    private Number clientCursors_size;
    private Number totalOpen;
    private Number pinned;
    private Number totalNoTimeout;
    private Number timedOut;

    public Number getClientCursors_size() {
        return this.clientCursors_size;
    }

    public void setClientCursors_size(Number clientCursors_size) {
        this.clientCursors_size = clientCursors_size;
    }

    public Number getTimedOut() {
        return this.timedOut;
    }

    public void setTimedOut(Number timedOut) {
        this.timedOut = timedOut;
    }

    public Number getTotalOpen() {
        return this.totalOpen;
    }

    public void setTotalOpen(Number totalOpen) {
        this.totalOpen = totalOpen;
    }

    public Number getPinned() {
        return pinned;
    }

    public void setPinned(Number pinned) {
        this.pinned = pinned;
    }

    public Number getTotalNoTimeout() {
        return totalNoTimeout;
    }

    public void setTotalNoTimeout(Number totalNoTimeout) {
        this.totalNoTimeout = totalNoTimeout;
    }
}
