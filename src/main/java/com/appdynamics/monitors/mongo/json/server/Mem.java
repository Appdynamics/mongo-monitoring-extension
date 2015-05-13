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

public class Mem {
    private Number bits;
    private Number resident;
    private Number virtual;
    private Number mapped;
    private Number mappedWithJournal;

    public Number getBits() {
        return bits;
    }

    public void setBits(Number bits) {
        this.bits = bits;
    }

    public Number getResident() {
        return resident;
    }

    public void setResident(Number resident) {
        this.resident = resident;
    }

    public Number getVirtual() {
        return virtual;
    }

    public void setVirtual(Number virtual) {
        this.virtual = virtual;
    }

    public Number getMapped() {
        return mapped;
    }

    public void setMapped(Number mapped) {
        this.mapped = mapped;
    }

    public Number getMappedWithJournal() {
        return mappedWithJournal;
    }

    public void setMappedWithJournal(Number mappedWithJournal) {
        this.mappedWithJournal = mappedWithJournal;
    }

}
