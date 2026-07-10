package rx.internal.operators;

import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;

/* JADX INFO: loaded from: classes.dex */
final class TakeLastQueueProducer<T> extends AtomicLong implements Producer {
    private final Deque<Object> deque;
    private volatile boolean emittingStarted = false;
    private final NotificationLite<T> notification;
    private final Subscriber<? super T> subscriber;

    public TakeLastQueueProducer(NotificationLite<T> n, Deque<Object> q, Subscriber<? super T> subscriber) {
        this.notification = n;
        this.deque = q;
        this.subscriber = subscriber;
    }

    void startEmitting() {
        if (!this.emittingStarted) {
            this.emittingStarted = true;
            emit(0L);
        }
    }

    @Override // rx.Producer
    public void request(long n) {
        long _c;
        if (get() != Long.MAX_VALUE) {
            if (n == Long.MAX_VALUE) {
                _c = getAndSet(Long.MAX_VALUE);
            } else {
                _c = BackpressureUtils.getAndAddRequest(this, n);
            }
            if (this.emittingStarted) {
                emit(_c);
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:34:0x009d, code lost:
    
        r10 = get();
        r6 = r10 - ((long) r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x00ab, code lost:
    
        if (r10 == Long.MAX_VALUE) goto L51;
     */
    /* JADX WARN: Code restructure failed: missing block: B:37:0x00b3, code lost:
    
        if (compareAndSet(r10, r6) == false) goto L56;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:0x00b9, code lost:
    
        if (r6 != 0) goto L52;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:?, code lost:
    
        return;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    void emit(long previousRequested) {
        Object o;
        if (get() == Long.MAX_VALUE) {
            if (previousRequested == 0) {
                try {
                    for (Object value : this.deque) {
                        if (!this.subscriber.isUnsubscribed()) {
                            this.notification.accept(this.subscriber, value);
                        } else {
                            return;
                        }
                    }
                    return;
                } catch (Throwable e) {
                    Exceptions.throwOrReport(e, this.subscriber);
                    return;
                } finally {
                    this.deque.clear();
                }
            }
            return;
        }
        if (previousRequested != 0) {
            return;
        }
        while (true) {
            long numToEmit = get();
            int emitted = 0;
            while (true) {
                numToEmit--;
                if (numToEmit < 0 || (o = this.deque.poll()) == null) {
                    break;
                }
                if (!this.subscriber.isUnsubscribed() && !this.notification.accept(this.subscriber, o)) {
                    emitted++;
                } else {
                    return;
                }
            }
        }
    }
}
