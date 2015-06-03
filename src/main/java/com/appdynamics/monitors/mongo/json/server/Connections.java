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

public class Connections {
    private Number available;
    private Number current;

    public Number getAvailable() {
        return this.available;
    }

    public void setAvailable(Number available) {
        this.available = available;
    }

    public Number getCurrent() {
        return this.current;
    }

    public void setCurrent(Number current) {
        this.current = current;
    }
}