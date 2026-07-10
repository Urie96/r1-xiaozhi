package rx.internal.operators;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import rx.Observable;
import rx.Observer;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.internal.util.atomic.SpscLinkedAtomicQueue;
import rx.internal.util.atomic.SpscUnboundedAtomicArrayQueue;
import rx.internal.util.unsafe.SpscLinkedQueue;
import rx.internal.util.unsafe.SpscUnboundedArrayQueue;
import rx.internal.util.unsafe.UnsafeAccess;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

/* JADX INFO: loaded from: classes.dex */
public final class UnicastSubject<T> extends Subject<T, T> {
    final State<T> state;

    public static <T> UnicastSubject<T> create() {
        return create(16);
    }

    public static <T> UnicastSubject<T> create(int capacityHint) {
        State<T> state = new State<>(capacityHint);
        return new UnicastSubject<>(state);
    }

    private UnicastSubject(State<T> state) {
        super(state);
        this.state = state;
    }

    @Override // rx.Observer
    public void onNext(T t) {
        this.state.onNext(t);
    }

    @Override // rx.Observer
    public void onError(Throwable e) {
        this.state.onError(e);
    }

    @Override // rx.Observer
    public void onCompleted() {
        this.state.onCompleted();
    }

    @Override // rx.subjects.Subject
    public boolean hasObservers() {
        return this.state.subscriber.get() != null;
    }

    static final class State<T> extends AtomicLong implements Producer, Observer<T>, Action0, Observable.OnSubscribe<T> {
        private static final long serialVersionUID = -9044104859202255786L;
        volatile boolean caughtUp;
        volatile boolean done;
        boolean emitting;
        Throwable error;
        boolean missed;
        final Queue<Object> queue;
        final NotificationLite<T> nl = NotificationLite.instance();
        final AtomicReference<Subscriber<? super T>> subscriber = new AtomicReference<>();

        public State(int capacityHint) {
            Queue<Object> q;
            if (capacityHint > 1) {
                q = UnsafeAccess.isUnsafeAvailable() ? new SpscUnboundedArrayQueue<>(capacityHint) : new SpscUnboundedAtomicArrayQueue<>(capacityHint);
            } else {
                q = UnsafeAccess.isUnsafeAvailable() ? new SpscLinkedQueue<>() : new SpscLinkedAtomicQueue<>();
            }
            this.queue = q;
        }

        @Override // rx.Observer
        public void onNext(T t) {
            if (!this.done) {
                if (!this.caughtUp) {
                    boolean stillReplay = false;
                    synchronized (this) {
                        if (!this.caughtUp) {
                            this.queue.offer(this.nl.next(t));
                            stillReplay = true;
                        }
                    }
                    if (stillReplay) {
                        replay();
                        return;
                    }
                }
                Subscriber<? super T> s = this.subscriber.get();
                try {
                    s.onNext(t);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    s.onError(OnErrorThrowable.addValueAsLastCause(ex, t));
                }
            }
        }

        @Override // rx.Observer
        public void onError(Throwable e) {
            boolean stillReplay;
            if (!this.done) {
                this.error = e;
                this.done = true;
                if (!this.caughtUp) {
                    synchronized (this) {
                        stillReplay = !this.caughtUp;
                    }
                    if (stillReplay) {
                        replay();
                        return;
                    }
                }
                this.subscriber.get().onError(e);
            }
        }

        @Override // rx.Observer
        public void onCompleted() {
            boolean stillReplay;
            if (!this.done) {
                this.done = true;
                if (!this.caughtUp) {
                    synchronized (this) {
                        stillReplay = !this.caughtUp;
                    }
                    if (stillReplay) {
                        replay();
                        return;
                    }
                }
                this.subscriber.get().onCompleted();
            }
        }

        @Override // rx.Producer
        public void request(long n) {
            if (n < 0) {
                throw new IllegalArgumentException("n >= 0 required");
            }
            if (n > 0) {
                BackpressureUtils.getAndAddRequest(this, n);
                replay();
            } else if (this.done) {
                replay();
            }
        }

        @Override // rx.functions.Action1
        public void call(Subscriber<? super T> subscriber) {
            if (this.subscriber.compareAndSet(null, subscriber)) {
                subscriber.add(Subscriptions.create(this));
                subscriber.setProducer(this);
            } else {
                subscriber.onError(new IllegalStateException("Only a single subscriber is allowed"));
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:34:0x005f, code lost:
        
            if (r9 == false) goto L38;
         */
        /* JADX WARN: Code restructure failed: missing block: B:36:0x0065, code lost:
        
            if (r5.isEmpty() == false) goto L38;
         */
        /* JADX WARN: Code restructure failed: missing block: B:37:0x0067, code lost:
        
            r14.caughtUp = true;
         */
        /* JADX WARN: Code restructure failed: missing block: B:38:0x006a, code lost:
        
            r14.emitting = false;
         */
        /* JADX WARN: Code restructure failed: missing block: B:68:?, code lost:
        
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void replay() {
            synchronized (this) {
                if (this.emitting) {
                    this.missed = true;
                    return;
                }
                this.emitting = true;
                Queue<Object> queue = this.queue;
                while (true) {
                    Subscriber<? super T> subscriber = this.subscriber.get();
                    boolean z = false;
                    if (subscriber != null) {
                        if (!checkTerminated(this.done, queue.isEmpty(), subscriber)) {
                            long j = get();
                            z = j == Long.MAX_VALUE;
                            long j2 = 0;
                            while (j != 0) {
                                boolean z2 = this.done;
                                Object objPoll = queue.poll();
                                boolean z3 = objPoll == null;
                                if (!checkTerminated(z2, z3, subscriber)) {
                                    if (z3) {
                                        break;
                                    }
                                    T value = this.nl.getValue(objPoll);
                                    try {
                                        subscriber.onNext(value);
                                        j--;
                                        j2++;
                                    } catch (Throwable th) {
                                        queue.clear();
                                        Exceptions.throwIfFatal(th);
                                        subscriber.onError(OnErrorThrowable.addValueAsLastCause(th, value));
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                            if (!z && j2 != 0) {
                                addAndGet(-j2);
                            }
                        } else {
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
                }
            }
        }

        @Override // rx.functions.Action0
        public void call() {
            this.done = true;
            synchronized (this) {
                if (!this.emitting) {
                    this.emitting = true;
                    this.queue.clear();
                }
            }
        }

        boolean checkTerminated(boolean done, boolean empty, Subscriber<? super T> s) {
            if (s.isUnsubscribed()) {
                this.queue.clear();
                return true;
            }
            if (done) {
                Throwable e = this.error;
                if (e != null) {
                    this.queue.clear();
                    s.onError(e);
                    return true;
                }
                if (empty) {
                    s.onCompleted();
                    return true;
                }
            }
            return false;
        }
    }
}
