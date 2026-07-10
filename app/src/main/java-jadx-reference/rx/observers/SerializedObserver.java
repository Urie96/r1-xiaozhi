package rx.observers;

import rx.Observer;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.internal.operators.NotificationLite;

/* JADX INFO: loaded from: classes.dex */
public class SerializedObserver<T> implements Observer<T> {
    private static final int MAX_DRAIN_ITERATION = 1024;
    private final Observer<? super T> actual;
    private boolean emitting;
    private final NotificationLite<T> nl = NotificationLite.instance();
    private FastList queue;
    private volatile boolean terminated;

    static final class FastList {
        Object[] array;
        int size;

        FastList() {
        }

        public void add(Object o) {
            int s = this.size;
            Object[] a2 = this.array;
            if (a2 == null) {
                a2 = new Object[16];
                this.array = a2;
            } else if (s == a2.length) {
                Object[] array2 = new Object[(s >> 2) + s];
                System.arraycopy(a2, 0, array2, 0, s);
                a2 = array2;
                this.array = a2;
            }
            a2[s] = o;
            this.size = s + 1;
        }
    }

    public SerializedObserver(Observer<? super T> s) {
        this.actual = s;
    }

    /* JADX WARN: Code restructure failed: missing block: B:64:0x0063, code lost:
    
        continue;
     */
    @Override // rx.Observer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void onNext(T t) {
        if (!this.terminated) {
            synchronized (this) {
                if (!this.terminated) {
                    if (this.emitting) {
                        FastList list = this.queue;
                        if (list == null) {
                            list = new FastList();
                            this.queue = list;
                        }
                        list.add(this.nl.next(t));
                        return;
                    }
                    this.emitting = true;
                    try {
                        this.actual.onNext(t);
                        while (true) {
                            for (int i = 0; i < 1024; i++) {
                                synchronized (this) {
                                    FastList list2 = this.queue;
                                    if (list2 == null) {
                                        this.emitting = false;
                                        return;
                                    }
                                    this.queue = null;
                                    Object[] arr$ = list2.array;
                                    for (Object o : arr$) {
                                        if (o == null) {
                                            break;
                                        }
                                        try {
                                            if (this.nl.accept(this.actual, o)) {
                                                this.terminated = true;
                                                return;
                                            }
                                        } catch (Throwable e) {
                                            this.terminated = true;
                                            Exceptions.throwIfFatal(e);
                                            this.actual.onError(OnErrorThrowable.addValueAsLastCause(e, t));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable e2) {
                        this.terminated = true;
                        Exceptions.throwIfFatal(e2);
                        this.actual.onError(OnErrorThrowable.addValueAsLastCause(e2, t));
                    }
                }
            }
        }
    }

    @Override // rx.Observer
    public void onError(Throwable e) {
        Exceptions.throwIfFatal(e);
        if (!this.terminated) {
            synchronized (this) {
                if (!this.terminated) {
                    this.terminated = true;
                    if (this.emitting) {
                        FastList list = this.queue;
                        if (list == null) {
                            list = new FastList();
                            this.queue = list;
                        }
                        list.add(this.nl.error(e));
                        return;
                    }
                    this.emitting = true;
                    this.actual.onError(e);
                }
            }
        }
    }

    @Override // rx.Observer
    public void onCompleted() {
        if (!this.terminated) {
            synchronized (this) {
                if (!this.terminated) {
                    this.terminated = true;
                    if (this.emitting) {
                        FastList list = this.queue;
                        if (list == null) {
                            list = new FastList();
                            this.queue = list;
                        }
                        list.add(this.nl.completed());
                        return;
                    }
                    this.emitting = true;
                    this.actual.onCompleted();
                }
            }
        }
    }
}
