package edu.ualberta.med.biobank.common.reports;

import java.util.Iterator;

public class BiobankListProxyIterator implements Iterator<Object> {

    BiobankListProxy proxy;
    int index;

    public BiobankListProxyIterator(BiobankListProxy proxy) {
        this.proxy = proxy;
        this.index = -1;
    }

    @Override
    public boolean hasNext() {
        return proxy.get(index + 1) != null;
    }

    @Override
    public Object next() {
        return proxy.get(++index);
    }

    @Override
    public void remove() {
    }

}