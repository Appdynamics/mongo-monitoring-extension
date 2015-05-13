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

public class TimeMs {
    private Number dt;
    private Number prepLogBuffer;
    private Number remapPrivateView;
    private Number writeToDataFiles;
    private Number writeToJournal;

    public Number getDt() {
        return this.dt;
    }

    public void setDt(Number dt) {
        this.dt = dt;
    }

    public Number getPrepLogBuffer() {
        return this.prepLogBuffer;
    }

    public void setPrepLogBuffer(Number prepLogBuffer) {
        this.prepLogBuffer = prepLogBuffer;
    }

    public Number getRemapPrivateView() {
        return this.remapPrivateView;
    }

    public void setRemapPrivateView(Number remapPrivateView) {
        this.remapPrivateView = remapPrivateView;
    }

    public Number getWriteToDataFiles() {
        return this.writeToDataFiles;
    }

    public void setWriteToDataFiles(Number writeToDataFiles) {
        this.writeToDataFiles = writeToDataFiles;
    }

    public Number getWriteToJournal() {
        return this.writeToJournal;
    }

    public void setWriteToJournal(Number writeToJournal) {
        this.writeToJournal = writeToJournal;
    }
}
