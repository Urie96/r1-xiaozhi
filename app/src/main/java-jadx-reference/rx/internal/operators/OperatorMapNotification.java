package rx.internal.operators;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.exceptions.MissingBackpressureException;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.internal.producers.ProducerArbiter;
import rx.internal.util.unsafe.SpscArrayQueue;
import rx.internal.util.unsafe.UnsafeAccess;

/* JADX INFO: loaded from: classes.dex */
public final class OperatorMapNotification<T, R> implements Observable.Operator<R, T> {
    private final Func0<? extends R> onCompleted;
    private final Func1<? super Throwable, ? extends R> onError;
    private final Func1<? super T, ? extends R> onNext;

    public OperatorMapNotification(Func1<? super T, ? extends R> onNext, Func1<? super Throwable, ? extends R> onError, Func0<? extends R> onCompleted) {
        this.onNext = onNext;
        this.onError = onError;
        this.onCompleted = onCompleted;
    }

    @Override // rx.functions.Func1
    public Subscriber<? super T> call(Subscriber<? super R> o) {
        ProducerArbiter pa = new ProducerArbiter();
        OperatorMapNotification<T, R>.MapNotificationSubscriber subscriber = new MapNotificationSubscriber(pa, o);
        o.add(subscriber);
        subscriber.init();
        return subscriber;
    }

    final class MapNotificationSubscriber extends Subscriber<T> {
        final SingleEmitter<R> emitter;
        private final Subscriber<? super R> o;
        private final ProducerArbiter pa;

        private MapNotificationSubscriber(ProducerArbiter pa, Subscriber<? super R> o) {
            this.pa = pa;
            this.o = o;
            this.emitter = new SingleEmitter<>(o, pa, this);
        }

        void init() {
            this.o.setProducer(this.emitter);
        }

        @Override // rx.Subscriber
        public void setProducer(Producer producer) {
            this.pa.setProducer(producer);
        }

        @Override // rx.Observer
        public void onCompleted() {
            try {
                this.emitter.offerAndComplete((R) OperatorMapNotification.this.onCompleted.call());
            } catch (Throwable th) {
                Exceptions.throwOrReport(th, this.o);
            }
        }

        @Override // rx.Observer
        public void onError(Throwable th) {
            try {
                this.emitter.offerAndComplete((R) OperatorMapNotification.this.onError.call(th));
            } catch (Throwable th2) {
                Exceptions.throwOrReport(th2, this.o);
            }
        }

        @Override // rx.Observer
        public void onNext(T t) {
            try {
                this.emitter.offer((R) OperatorMapNotification.this.onNext.call(t));
            } catch (Throwable th) {
                Exceptions.throwOrReport(th, this.o, t);
            }
        }
    }

    static final class SingleEmitter<T> extends AtomicLong implements Producer, Subscription {
        private static final long serialVersionUID = -249869671366010660L;
        final Subscription cancel;
        final Subscriber<? super T> child;
        volatile boolean complete;
        boolean emitting;
        boolean missed;
        final NotificationLite<T> nl;
        final Producer producer;
        final Queue<Object> queue;

        public SingleEmitter(Subscriber<? super T> child, Producer producer, Subscription cancel) {
            this.child = child;
            this.producer = producer;
            this.cancel = cancel;
            this.queue = UnsafeAccess.isUnsafeAvailable() ? new SpscArrayQueue<>(2) : new ConcurrentLinkedQueue<>();
            this.nl = NotificationLite.instance();
        }

        @Override // rx.Producer
        public void request(long n) {
            long r;
            long u;
            do {
                r = get();
                if (r >= 0) {
                    u = r + n;
                    if (u < 0) {
                        u = Long.MAX_VALUE;
                    }
                } else {
                    return;
                }
            } while (!compareAndSet(r, u));
            this.producer.request(n);
            drain();
        }

        void produced(long n) {
            long r;
            long u;
            do {
                r = get();
                if (r >= 0) {
                    u = r - n;
                    if (u < 0) {
                        throw new IllegalStateException("More produced (" + n + ") than requested (" + r + ")");
                    }
                } else {
                    return;
                }
            } while (!compareAndSet(r, u));
        }

        public void offer(T value) {
            if (!this.queue.offer(value)) {
                this.child.onError(new MissingBackpressureException());
                unsubscribe();
            } else {
                drain();
            }
        }

        public void offerAndComplete(T value) {
            if (!this.queue.offer(value)) {
                this.child.onError(new MissingBackpressureException());
                unsubscribe();
            } else {
                this.complete = true;
                drain();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:36:0x0059, code lost:
        
            r4 = true;
            r8.emitting = false;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void drain() {
            synchronized (this) {
                if (this.emitting) {
                    this.missed = true;
                    return;
                }
                this.emitting = true;
                this.missed = false;
                boolean z = false;
                while (true) {
                    try {
                        long j = get();
                        boolean z2 = this.complete;
                        boolean zIsEmpty = this.queue.isEmpty();
                        if (z2 && zIsEmpty) {
                            this.child.onCompleted();
                            if (1 == 0) {
                                synchronized (this) {
                                    this.emitting = false;
                                }
                                return;
                            }
                            return;
                        }
                        if (j > 0) {
                            Object objPoll = this.queue.poll();
                            if (objPoll != null) {
                                this.child.onNext(this.nl.getValue(objPoll));
                                produced(1L);
                            } else if (z2) {
                                this.child.onCompleted();
                                if (1 == 0) {
                                    synchronized (this) {
                                        this.emitting = false;
                                    }
                                    return;
                                }
                                return;
                            }
                        }
                        synchronized (this) {
                            if (!this.missed) {
                                break;
                            } else {
                                this.missed = false;
                            }
                        }
                        if (1 == 0) {
                            synchronized (this) {
                                this.emitting = false;
                            }
                            return;
                        }
                        return;
                    } catch (Throwable th) {
                        if (!z) {
                            synchronized (this) {
                                this.emitting = false;
                            }
                        }
                        throw th;
                    }
                }
            }
        }

        @Override // rx.Subscription
        public boolean isUnsubscribed() {
            return get() < 0;
        }

        @Override // rx.Subscription
        public void unsubscribe() {
            long r = get();
            if (r != Long.MIN_VALUE) {
                long r2 = getAndSet(Long.MIN_VALUE);
                if (r2 != Long.MIN_VALUE) {
                    this.cancel.unsubscribe();
                }
            }
        }
    }
}
