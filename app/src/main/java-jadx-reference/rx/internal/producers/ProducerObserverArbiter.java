package rx.internal.producers;

import java.util.ArrayList;
import java.util.List;
import rx.Observer;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.internal.operators.BackpressureUtils;

/* JADX INFO: loaded from: classes.dex */
public final class ProducerObserverArbiter<T> implements Producer, Observer<T> {
    static final Producer NULL_PRODUCER = new Producer() { // from class: rx.internal.producers.ProducerObserverArbiter.1
        @Override // rx.Producer
        public void request(long n) {
        }
    };
    final Subscriber<? super T> child;
    Producer currentProducer;
    boolean emitting;
    volatile boolean hasError;
    Producer missedProducer;
    long missedRequested;
    Object missedTerminal;
    List<T> queue;
    long requested;

    public ProducerObserverArbiter(Subscriber<? super T> child) {
        this.child = child;
    }

    @Override // rx.Observer
    public void onNext(T t) {
        synchronized (this) {
            if (this.emitting) {
                List<T> q = this.queue;
                if (q == null) {
                    q = new ArrayList<>(4);
                    this.queue = q;
                }
                q.add(t);
                return;
            }
            try {
                this.child.onNext(t);
                long r = this.requested;
                if (r != Long.MAX_VALUE) {
                    this.requested = r - 1;
                }
                emitLoop();
                if (1 == 0) {
                    synchronized (this) {
                        this.emitting = false;
                    }
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    synchronized (this) {
                        this.emitting = false;
                    }
                }
                throw th;
            }
        }
    }

    @Override // rx.Observer
    public void onError(Throwable e) {
        boolean emit;
        synchronized (this) {
            if (this.emitting) {
                this.missedTerminal = e;
                emit = false;
            } else {
                this.emitting = true;
                emit = true;
            }
        }
        if (emit) {
            this.child.onError(e);
        } else {
            this.hasError = true;
        }
    }

    @Override // rx.Observer
    public void onCompleted() {
        synchronized (this) {
            if (this.emitting) {
                this.missedTerminal = true;
            } else {
                this.emitting = true;
                this.child.onCompleted();
            }
        }
    }

    @Override // rx.Producer
    public void request(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("n >= 0 required");
        }
        if (n != 0) {
            synchronized (this) {
                if (this.emitting) {
                    this.missedRequested += n;
                } else {
                    this.emitting = true;
                    Producer p = this.currentProducer;
                    try {
                        long r = this.requested;
                        long u = r + n;
                        if (u < 0) {
                            u = Long.MAX_VALUE;
                        }
                        this.requested = u;
                        emitLoop();
                        if (1 == 0) {
                            synchronized (this) {
                                this.emitting = false;
                            }
                        }
                        if (p != null) {
                            p.request(n);
                        }
                    } catch (Throwable th) {
                        if (0 == 0) {
                            synchronized (this) {
                                this.emitting = false;
                            }
                        }
                        throw th;
                    }
                }
            }
        }
    }

    public void setProducer(Producer p) {
        synchronized (this) {
            if (this.emitting) {
                if (p == null) {
                    p = NULL_PRODUCER;
                }
                this.missedProducer = p;
                return;
            }
            this.emitting = true;
            this.currentProducer = p;
            long r = this.requested;
            try {
                emitLoop();
                if (1 == 0) {
                    synchronized (this) {
                        this.emitting = false;
                    }
                }
                if (p != null && r != 0) {
                    p.request(r);
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    synchronized (this) {
                        this.emitting = false;
                    }
                }
                throw th;
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:97:0x0008, code lost:
    
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void emitLoop() {
        long j;
        Producer producer;
        Object obj;
        List<T> list;
        Subscriber<? super T> subscriber = this.child;
        long jAddCap = 0;
        Producer producer2 = null;
        while (true) {
            boolean z = false;
            synchronized (this) {
                j = this.missedRequested;
                producer = this.missedProducer;
                obj = this.missedTerminal;
                list = this.queue;
                if (j == 0 && producer == null && list == null && obj == null) {
                    this.emitting = false;
                    z = true;
                } else {
                    this.missedRequested = 0L;
                    this.missedProducer = null;
                    this.queue = null;
                    this.missedTerminal = null;
                }
            }
            if (z) {
                if (jAddCap != 0 && producer2 != null) {
                    producer2.request(jAddCap);
                    return;
                }
                return;
            }
            boolean z2 = list == null || list.isEmpty();
            if (obj != null) {
                if (obj != Boolean.TRUE) {
                    subscriber.onError((Throwable) obj);
                    return;
                } else if (z2) {
                    subscriber.onCompleted();
                    return;
                }
            }
            long size = 0;
            if (list != null) {
                for (T t : list) {
                    if (!subscriber.isUnsubscribed()) {
                        if (!this.hasError) {
                            try {
                                subscriber.onNext(t);
                            } catch (Throwable th) {
                                Exceptions.throwOrReport(th, subscriber, t);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
                size = 0 + ((long) list.size());
            }
            long j2 = this.requested;
            if (j2 != Long.MAX_VALUE) {
                if (j != 0) {
                    long j3 = j2 + j;
                    if (j3 < 0) {
                        j3 = Long.MAX_VALUE;
                    }
                    j2 = j3;
                }
                if (size != 0 && j2 != Long.MAX_VALUE) {
                    long j4 = j2 - size;
                    if (j4 < 0) {
                        throw new IllegalStateException("More produced than requested");
                    }
                    j2 = j4;
                }
                this.requested = j2;
            }
            if (producer != null) {
                if (producer == NULL_PRODUCER) {
                    this.currentProducer = null;
                } else {
                    this.currentProducer = producer;
                    if (j2 != 0) {
                        jAddCap = BackpressureUtils.addCap(jAddCap, j2);
                        producer2 = producer;
                    }
                }
            } else {
                Producer producer3 = this.currentProducer;
                if (producer3 != null && j != 0) {
                    jAddCap = BackpressureUtils.addCap(jAddCap, j);
                    producer2 = producer3;
                }
            }
        }
    }
}
