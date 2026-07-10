package rx.internal.operators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.internal.util.atomic.SpscAtomicArrayQueue;
import rx.internal.util.unsafe.SpscArrayQueue;
import rx.internal.util.unsafe.UnsafeAccess;
import rx.subscriptions.Subscriptions;

/* JADX INFO: loaded from: classes.dex */
public final class OperatorEagerConcatMap<T, R> implements Observable.Operator<R, T> {
    final int bufferSize;
    final Func1<? super T, ? extends Observable<? extends R>> mapper;

    public OperatorEagerConcatMap(Func1<? super T, ? extends Observable<? extends R>> mapper, int bufferSize) {
        this.mapper = mapper;
        this.bufferSize = bufferSize;
    }

    @Override // rx.functions.Func1
    public Subscriber<? super T> call(Subscriber<? super R> t) {
        EagerOuterSubscriber<T, R> outer = new EagerOuterSubscriber<>(this.mapper, this.bufferSize, t);
        outer.init();
        return outer;
    }

    static final class EagerOuterProducer extends AtomicLong implements Producer {
        private static final long serialVersionUID = -657299606803478389L;
        final EagerOuterSubscriber<?, ?> parent;

        public EagerOuterProducer(EagerOuterSubscriber<?, ?> parent) {
            this.parent = parent;
        }

        @Override // rx.Producer
        public void request(long n) {
            if (n < 0) {
                throw new IllegalStateException("n >= 0 required but it was " + n);
            }
            if (n > 0) {
                BackpressureUtils.getAndAddRequest(this, n);
                this.parent.drain();
            }
        }
    }

    static final class EagerOuterSubscriber<T, R> extends Subscriber<T> {
        final Subscriber<? super R> actual;
        final int bufferSize;
        volatile boolean cancelled;
        volatile boolean done;
        Throwable error;
        final Func1<? super T, ? extends Observable<? extends R>> mapper;
        private EagerOuterProducer sharedProducer;
        final LinkedList<EagerInnerSubscriber<R>> subscribers = new LinkedList<>();
        final AtomicInteger wip = new AtomicInteger();

        public EagerOuterSubscriber(Func1<? super T, ? extends Observable<? extends R>> mapper, int bufferSize, Subscriber<? super R> actual) {
            this.mapper = mapper;
            this.bufferSize = bufferSize;
            this.actual = actual;
        }

        void init() {
            this.sharedProducer = new EagerOuterProducer(this);
            add(Subscriptions.create(new Action0() { // from class: rx.internal.operators.OperatorEagerConcatMap.EagerOuterSubscriber.1
                @Override // rx.functions.Action0
                public void call() {
                    EagerOuterSubscriber.this.cancelled = true;
                    if (EagerOuterSubscriber.this.wip.getAndIncrement() == 0) {
                        EagerOuterSubscriber.this.cleanup();
                    }
                }
            }));
            this.actual.add(this);
            this.actual.setProducer(this.sharedProducer);
        }

        void cleanup() {
            List<Subscription> list;
            synchronized (this.subscribers) {
                list = new ArrayList<>(this.subscribers);
                this.subscribers.clear();
            }
            for (Subscription s : list) {
                s.unsubscribe();
            }
        }

        @Override // rx.Observer
        public void onNext(T t) {
            try {
                Observable<? extends R> observable = this.mapper.call(t);
                EagerInnerSubscriber<R> inner = new EagerInnerSubscriber<>(this, this.bufferSize);
                if (!this.cancelled) {
                    synchronized (this.subscribers) {
                        if (!this.cancelled) {
                            this.subscribers.add(inner);
                            if (!this.cancelled) {
                                observable.unsafeSubscribe(inner);
                                drain();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Exceptions.throwOrReport(e, this.actual, t);
            }
        }

        @Override // rx.Observer
        public void onError(Throwable e) {
            this.error = e;
            this.done = true;
            drain();
        }

        @Override // rx.Observer
        public void onCompleted() {
            this.done = true;
            drain();
        }

        /* JADX WARN: Removed duplicated region for block: B:58:0x00c8  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void drain() {
            EagerInnerSubscriber<R> eagerInnerSubscriberPeek;
            if (this.wip.getAndIncrement() == 0) {
                int iAddAndGet = 1;
                EagerOuterProducer eagerOuterProducer = this.sharedProducer;
                Subscriber<? super R> subscriber = this.actual;
                while (!this.cancelled) {
                    boolean z = this.done;
                    synchronized (this.subscribers) {
                        eagerInnerSubscriberPeek = this.subscribers.peek();
                    }
                    boolean z2 = eagerInnerSubscriberPeek == null;
                    if (z) {
                        Throwable th = this.error;
                        if (th != null) {
                            cleanup();
                            subscriber.onError(th);
                            return;
                        } else if (z2) {
                            subscriber.onCompleted();
                            return;
                        }
                    }
                    if (!z2) {
                        long j = eagerOuterProducer.get();
                        long j2 = 0;
                        boolean z3 = j == Long.MAX_VALUE;
                        Queue<R> queue = eagerInnerSubscriberPeek.queue;
                        boolean z4 = false;
                        while (true) {
                            boolean z5 = eagerInnerSubscriberPeek.done;
                            R rPeek = queue.peek();
                            boolean z6 = rPeek == null;
                            if (z5) {
                                Throwable th2 = eagerInnerSubscriberPeek.error;
                                if (th2 != null) {
                                    cleanup();
                                    subscriber.onError(th2);
                                    return;
                                } else if (z6) {
                                    synchronized (this.subscribers) {
                                        this.subscribers.poll();
                                    }
                                    eagerInnerSubscriberPeek.unsubscribe();
                                    z4 = true;
                                    break;
                                }
                            } else {
                                if (z6 || j == 0) {
                                    break;
                                }
                                queue.poll();
                                try {
                                    subscriber.onNext(rPeek);
                                    j--;
                                    j2--;
                                } catch (Throwable th3) {
                                    Exceptions.throwOrReport(th3, subscriber, rPeek);
                                    return;
                                }
                            }
                        }
                        if (j2 != 0) {
                            if (!z3) {
                                eagerOuterProducer.addAndGet(j2);
                            }
                            if (!z4) {
                                eagerInnerSubscriberPeek.requestMore(-j2);
                            }
                        }
                        if (z4) {
                            continue;
                        }
                    }
                    iAddAndGet = this.wip.addAndGet(-iAddAndGet);
                    if (iAddAndGet == 0) {
                        return;
                    }
                }
                cleanup();
            }
        }
    }

    static final class EagerInnerSubscriber<T> extends Subscriber<T> {
        volatile boolean done;
        Throwable error;
        final EagerOuterSubscriber<?, T> parent;
        final Queue<T> queue;

        public EagerInnerSubscriber(EagerOuterSubscriber<?, T> parent, int bufferSize) {
            Queue<T> q;
            this.parent = parent;
            if (UnsafeAccess.isUnsafeAvailable()) {
                q = new SpscArrayQueue<>(bufferSize);
            } else {
                q = new SpscAtomicArrayQueue<>(bufferSize);
            }
            this.queue = q;
            request(bufferSize);
        }

        @Override // rx.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            this.parent.drain();
        }

        @Override // rx.Observer
        public void onError(Throwable e) {
            this.error = e;
            this.done = true;
            this.parent.drain();
        }

        @Override // rx.Observer
        public void onCompleted() {
            this.done = true;
            this.parent.drain();
        }

        void requestMore(long n) {
            request(n);
        }
    }
}
