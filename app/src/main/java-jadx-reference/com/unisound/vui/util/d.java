package com.unisound.vui.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/* JADX INFO: loaded from: classes.dex */
public class d implements AttributeMap {
    private static final int BUCKET_SIZE = 4;
    private static final int MASK = 3;
    private static final AtomicReferenceFieldUpdater<d, AtomicReferenceArray> UPDATER = AtomicReferenceFieldUpdater.newUpdater(d.class, AtomicReferenceArray.class, "attributes");
    private volatile AtomicReferenceArray<a<?>> attributes;

    private static final class a<T> extends AtomicReference<T> implements Attribute<T> {

        /* JADX INFO: renamed from: a, reason: collision with root package name */
        private final a<?> f443a;
        private final AttributeKey<T> b;
        private a<?> c;
        private a<?> d;
        private volatile boolean e;

        /* JADX WARN: Multi-variable type inference failed */
        a(AttributeKey<T> attributeKey) {
            this.f443a = this;
            this.b = attributeKey;
        }

        a(a<?> aVar, AttributeKey<T> attributeKey) {
            this.f443a = aVar;
            this.b = attributeKey;
        }

        private void a() {
            synchronized (this.f443a) {
                if (this.c != null) {
                    this.c.d = this.d;
                    if (this.d != null) {
                        this.d.c = this.c;
                    }
                }
            }
        }

        @Override // com.unisound.vui.util.Attribute
        public T getAndRemove() {
            this.e = true;
            T andSet = getAndSet(null);
            a();
            return andSet;
        }

        @Override // com.unisound.vui.util.Attribute
        public AttributeKey<T> key() {
            return this.b;
        }

        @Override // com.unisound.vui.util.Attribute
        public void remove() {
            this.e = true;
            set(null);
            a();
        }

        @Override // com.unisound.vui.util.Attribute
        public T setIfAbsent(T value) {
            while (!compareAndSet(null, value)) {
                T t = get();
                if (t != null) {
                    return t;
                }
            }
            return null;
        }
    }

    private static int index(AttributeKey<?> key) {
        return key.id() & 3;
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x003f  */
    @Override // com.unisound.vui.util.AttributeMap
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        AtomicReferenceArray<a<?>> atomicReferenceArray;
        a<?> aVar;
        if (key == null) {
            throw new NullPointerException("key");
        }
        AtomicReferenceArray<a<?>> atomicReferenceArray2 = this.attributes;
        if (atomicReferenceArray2 == null) {
            AtomicReferenceArray<a<?>> atomicReferenceArray3 = new AtomicReferenceArray<>(4);
            atomicReferenceArray = !UPDATER.compareAndSet(this, null, atomicReferenceArray3) ? this.attributes : atomicReferenceArray3;
        } else {
            atomicReferenceArray = atomicReferenceArray2;
        }
        int iIndex = index(key);
        a<?> aVar2 = atomicReferenceArray.get(iIndex);
        if (aVar2 == null) {
            aVar = new a<>(key);
            if (!atomicReferenceArray.compareAndSet(iIndex, null, aVar)) {
                aVar2 = atomicReferenceArray.get(iIndex);
                synchronized (aVar2) {
                    a<?> aVar3 = aVar2;
                    while (true) {
                        if (!((a) aVar3).e && ((a) aVar3).b == key) {
                            aVar = aVar3;
                            break;
                        }
                        a<?> aVar4 = ((a) aVar3).d;
                        if (aVar4 == null) {
                            a<?> aVar5 = new a<>(aVar2, key);
                            ((a) aVar3).d = aVar5;
                            ((a) aVar5).c = aVar3;
                            aVar = aVar5;
                            break;
                        }
                        aVar3 = aVar4;
                    }
                }
            }
        }
        return aVar;
    }

    @Override // com.unisound.vui.util.AttributeMap
    public <T> boolean hasAttr(AttributeKey<T> key) {
        a<?> aVar;
        if (key == null) {
            throw new NullPointerException("key");
        }
        AtomicReferenceArray<a<?>> atomicReferenceArray = this.attributes;
        if (atomicReferenceArray != null && (aVar = atomicReferenceArray.get(index(key))) != null) {
            if (((a) aVar).b == key && !((a) aVar).e) {
                return true;
            }
            synchronized (aVar) {
                for (a aVar2 = ((a) aVar).d; aVar2 != null; aVar2 = aVar2.d) {
                    if (!aVar2.e && aVar2.b == key) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
