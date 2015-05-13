package com.appdynamics.monitors.mongo.json.db;

public class DBStats {

    private String db;
    private Number ok;
    private Number collections;
    private Number objects;
    private Number avgObjSize;
    private Number dataSize;
    private Number storageSize;
    private Number numExtents;
    private Number indexes;
    private Number indexSize;
    private Number fileSize;
    private Number nsSizeMB;

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public Number getOk() {
        return ok;
    }

    public void setOk(Number ok) {
        this.ok = ok;
    }

    public Number getCollections() {
        return collections;
    }

    public void setCollections(Number collections) {
        this.collections = collections;
    }

    public Number getObjects() {
        return objects;
    }

    public void setObjects(Number objects) {
        this.objects = objects;
    }

    public Number getAvgObjSize() {
        return avgObjSize;
    }

    public void setAvgObjSize(Number avgObjSize) {
        this.avgObjSize = avgObjSize;
    }

    public Number getDataSize() {
        return dataSize;
    }

    public void setDataSize(Number dataSize) {
        this.dataSize = dataSize;
    }

    public Number getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(Number storageSize) {
        this.storageSize = storageSize;
    }

    public Number getNumExtents() {
        return numExtents;
    }

    public void setNumExtents(Number numExtents) {
        this.numExtents = numExtents;
    }

    public Number getIndexes() {
        return indexes;
    }

    public void setIndexes(Number indexes) {
        this.indexes = indexes;
    }

    public Number getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(Number indexSize) {
        this.indexSize = indexSize;
    }

    public Number getFileSize() {
        return fileSize;
    }

    public void setFileSize(Number fileSize) {
        this.fileSize = fileSize;
    }

    public Number getNsSizeMB() {
        return nsSizeMB;
    }

    public void setNsSizeMB(Number nsSizeMB) {
        this.nsSizeMB = nsSizeMB;
    }
}
