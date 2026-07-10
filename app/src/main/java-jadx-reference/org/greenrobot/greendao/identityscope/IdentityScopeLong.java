package org.greenrobot.greendao.identityscope;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;
import org.greenrobot.greendao.internal.LongHashMap;

/* JADX INFO: loaded from: classes.dex */
public class IdentityScopeLong<T> implements IdentityScope<Long, T> {
    private final LongHashMap<Reference<T>> map = new LongHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public T get(Long key) {
        return get2(key.longValue());
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public T getNoLock(Long key) {
        return get2NoLock(key.longValue());
    }

    public T get2(long key) {
        this.lock.lock();
        try {
            Reference<T> ref = this.map.get(key);
            if (ref != null) {
                return ref.get();
            }
            return null;
        } finally {
            this.lock.unlock();
        }
    }

    public T get2NoLock(long key) {
        Reference<T> ref = this.map.get(key);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void put(Long key, T entity) {
        put2(key.longValue(), entity);
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void putNoLock(Long key, T entity) {
        put2NoLock(key.longValue(), entity);
    }

    public void put2(long key, T entity) {
        this.lock.lock();
        try {
            this.map.put(key, new WeakReference(entity));
        } finally {
            this.lock.unlock();
        }
    }

    public void put2NoLock(long key, T entity) {
        this.map.put(key, new WeakReference(entity));
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public boolean detach(Long key, T entity) {
        ReentrantLock reentrantLock;
        this.lock.lock();
        try {
            if (get(key) == entity && entity != null) {
                remove(key);
                return true;
            }
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void remove(Long key) {
        this.lock.lock();
        try {
            this.map.remove(key.longValue());
        } finally {
            this.lock.unlock();
        }
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void remove(Iterable<Long> keys) {
        this.lock.lock();
        try {
            for (Long key : keys) {
                this.map.remove(key.longValue());
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void clear() {
        this.lock.lock();
        try {
            this.map.clear();
        } finally {
            this.lock.unlock();
        }
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void lock() {
        this.lock.lock();
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void unlock() {
        this.lock.unlock();
    }

    @Override // org.greenrobot.greendao.identityscope.IdentityScope
    public void reserveRoom(int count) {
        this.map.reserveRoom(count);
    }
}
