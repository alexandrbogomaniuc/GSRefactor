package com.abs.casino.common.util.xml.xstreampool;

import com.thoughtworks.xstream.XStream;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * User: flsh
 * Date: 06.12.14.
 */
class SoftReferenceQueue implements Queue<XStream> {
    private Queue<SoftReference<XStream>> delegate;

    public SoftReferenceQueue (Queue<?> delegate) {
        this.delegate = (Queue<SoftReference<XStream>>)delegate;
    }

    public XStream poll () {
        XStream res;
        SoftReference<XStream> ref;
        while((ref = delegate.poll()) != null) {
            if((res = ref.get()) != null) {
                return res;
            }
        }
        return null;
    }

    public boolean offer (XStream e) {
        return delegate.offer(new SoftReference(e));
    }

    public boolean add (XStream e) {
        return delegate.add(new SoftReference(e));
    }

    public int size () {
        return delegate.size();
    }

    public boolean isEmpty () {
        return delegate.isEmpty();
    }

    public boolean contains (Object o) {
        return delegate.contains(o);
    }

    public void clear () {
        delegate.clear();
    }

    public boolean equals (Object o) {
        return delegate.equals(o);
    }

    public int hashCode () {
        return delegate.hashCode();
    }

    @Override
    public String toString () {
        return getClass().getSimpleName() + super.toString();
    }

    public Iterator<XStream> iterator () {
        throw new UnsupportedOperationException();
    }

    public XStream remove () {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray () {
        throw new UnsupportedOperationException();
    }

    public XStream element () {
        throw new UnsupportedOperationException();
    }

    public XStream peek () {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray (T[] a) {
        throw new UnsupportedOperationException();
    }

    public boolean remove (Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll (Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll (Collection<? extends XStream> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll (Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll (Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
