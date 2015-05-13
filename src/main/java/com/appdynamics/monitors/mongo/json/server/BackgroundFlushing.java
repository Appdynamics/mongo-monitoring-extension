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

public class BackgroundFlushing {
    private Number average_ms;
    private Number flushes;
    private Last_finished last_finished;
    private Number last_ms;
    private Number total_ms;

    public Number getAverage_ms() {
        return this.average_ms;
    }

    public void setAverage_ms(Number average_ms) {
        this.average_ms = average_ms;
    }

    public Number getFlushes() {
        return this.flushes;
    }

    public void setFlushes(Number flushes) {
        this.flushes = flushes;
    }

    public Last_finished getLast_finished() {
        return this.last_finished;
    }

    public void setLast_finished(Last_finished last_finished) {
        this.last_finished = last_finished;
    }

    public Number getLast_ms() {
        return this.last_ms;
    }

    public void setLast_ms(Number last_ms) {
        this.last_ms = last_ms;
    }

    public Number getTotal_ms() {
        return this.total_ms;
    }

    public void setTotal_ms(Number total_ms) {
        this.total_ms = total_ms;
    }
}
