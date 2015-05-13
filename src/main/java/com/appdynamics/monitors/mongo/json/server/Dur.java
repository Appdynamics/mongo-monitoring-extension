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

public class Dur {
    private Number commits;
    private Number journaledMB;
    private Number writeToDataFilesMB;
    private Number compression;
    private Number commitsInWriteLock;
    private Number earlyCommits;
    private TimeMs timeMs;

    public Number getCommits() {
        return this.commits;
    }

    public void setCommits(Number commits) {
        this.commits = commits;
    }

    public Number getCommitsInWriteLock() {
        return this.commitsInWriteLock;
    }

    public void setCommitsInWriteLock(Number commitsInWriteLock) {
        this.commitsInWriteLock = commitsInWriteLock;
    }

    public Number getCompression() {
        return this.compression;
    }

    public void setCompression(Number compression) {
        this.compression = compression;
    }

    public Number getEarlyCommits() {
        return this.earlyCommits;
    }

    public void setEarlyCommits(Number earlyCommits) {
        this.earlyCommits = earlyCommits;
    }

    public Number getJournaledMB() {
        return this.journaledMB;
    }

    public void setJournaledMB(Number journaledMB) {
        this.journaledMB = journaledMB;
    }

    public TimeMs getTimeMs() {
        return this.timeMs;
    }

    public void setTimeMs(TimeMs timeMs) {
        this.timeMs = timeMs;
    }

    public Number getWriteToDataFilesMB() {
        return this.writeToDataFilesMB;
    }

    public void setWriteToDataFilesMB(Number writeToDataFilesMB) {
        this.writeToDataFilesMB = writeToDataFilesMB;
    }
}
