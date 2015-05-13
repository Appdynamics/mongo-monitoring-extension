package com.appdynamics.monitors.mongo.json.db;

import java.util.Map;

public class CollectionStats {

    private String ns;
    private Number count;
    private Number size;
    private Number storageSize;
    private Number numExtents;
    private Number nindexes;
    private Number lastExtentSize;
    private Number paddingFactor;
    private Number systemFlags;
    private Number userFlags;
    private Number totalIndexSize;
    private Map<String, Number> indexSizes;

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    public Number getSize() {
        return size;
    }

    public void setSize(Number size) {
        this.size = size;
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

    public Number getNindexes() {
        return nindexes;
    }

    public void setNindexes(Number nindexes) {
        this.nindexes = nindexes;
    }

    public Number getLastExtentSize() {
        return lastExtentSize;
    }

    public void setLastExtentSize(Number lastExtentSize) {
        this.lastExtentSize = lastExtentSize;
    }

    public Number getPaddingFactor() {
        return paddingFactor;
    }

    public void setPaddingFactor(Number paddingFactor) {
        this.paddingFactor = paddingFactor;
    }

    public Number getSystemFlags() {
        return systemFlags;
    }

    public void setSystemFlags(Number systemFlags) {
        this.systemFlags = systemFlags;
    }

    public Number getUserFlags() {
        return userFlags;
    }

    public void setUserFlags(Number userFlags) {
        this.userFlags = userFlags;
    }

    public Number getTotalIndexSize() {
        return totalIndexSize;
    }

    public void setTotalIndexSize(Number totalIndexSize) {
        this.totalIndexSize = totalIndexSize;
    }

    public Map<String, Number> getIndexSizes() {
        return indexSizes;
    }

    public void setIndexSizes(Map<String, Number> indexSizes) {
        this.indexSizes = indexSizes;
    }
}
