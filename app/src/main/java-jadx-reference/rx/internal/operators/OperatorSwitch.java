package rx.internal.operators;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.internal.producers.ProducerArbiter;
import rx.observers.SerializedSubscriber;
import rx.subscriptions.SerialSubscription;

/* JADX INFO: loaded from: classes.dex */
public final class OperatorSwitch<T> implements Observable.Operator<T, Observable<? extends T>> {

    private static final class Holder {
        static final OperatorSwitch<Object> INSTANCE = new OperatorSwitch<>();

        private Holder() {
        }
    }

    public static <T> OperatorSwitch<T> instance() {
        return (OperatorSwitch<T>) Holder.INSTANCE;
    }

    private OperatorSwitch() {
    }

    @Override // rx.functions.Func1
    public Subscriber<? super Observable<? extends T>> call(Subscriber<? super T> child) {
        SwitchSubscriber<T> sws = new SwitchSubscriber<>(child);
        child.add(sws);
        return sws;
    }

    private static final class SwitchSubscriber<T> extends Subscriber<Observable<? extends T>> {
        boolean active;
        InnerSubscriber<T> currentSubscriber;
        boolean emitting;
        int index;
        boolean mainDone;
        List<Object> queue;
        final SerializedSubscriber<T> serializedChild;
        final Object guard = new Object();
        final NotificationLite<?> nl = NotificationLite.instance();
        final ProducerArbiter arbiter = new ProducerArbiter();
        final SerialSubscription ssub = new SerialSubscription();

        SwitchSubscriber(Subscriber<? super T> child) {
            this.serializedChild = new SerializedSubscriber<>(child);
            child.add(this.ssub);
            child.setProducer(new Producer() { // from class: rx.internal.operators.OperatorSwitch.SwitchSubscriber.1
                @Override // rx.Producer
                public void request(long n) {
                    if (n > 0) {
                        SwitchSubscriber.this.arbiter.request(n);
                    }
                }
            });
        }

        @Override // rx.Observer
        public void onNext(Observable<? extends T> t) {
            synchronized (this.guard) {
                int id = this.index + 1;
                this.index = id;
                this.active = true;
                this.currentSubscriber = new InnerSubscriber<>(id, this.arbiter, this);
            }
            this.ssub.set(this.currentSubscriber);
            t.unsafeSubscribe(this.currentSubscriber);
        }

        @Override // rx.Observer
        public void onError(Throwable e) {
            this.serializedChild.onError(e);
            unsubscribe();
        }

        @Override // rx.Observer
        public void onCompleted() {
            synchronized (this.guard) {
                this.mainDone = true;
                if (!this.active) {
                    if (this.emitting) {
                        if (this.queue == null) {
                            this.queue = new ArrayList();
                        }
                        this.queue.add(this.nl.completed());
                    } else {
                        List<Object> localQueue = this.queue;
                        this.queue = null;
                        this.emitting = true;
                        drain(localQueue);
                        this.serializedChild.onCompleted();
                        unsubscribe();
                    }
                }
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:29:0x0049, code lost:
        
            r6.emitting = false;
            r2 = true;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        void emit(T value, int id, InnerSubscriber<T> innerSubscriber) {
            synchronized (this.guard) {
                if (id == this.index) {
                    if (this.emitting) {
                        if (this.queue == null) {
                            this.queue = new ArrayList();
                        }
                        this.queue.add(value);
                        return;
                    }
                    List<Object> localQueue = this.queue;
                    this.queue = null;
                    this.emitting = true;
                    boolean once = true;
                    boolean skipFinal = false;
                    while (true) {
                        try {
                            drain(localQueue);
                            if (once) {
                                once = false;
                                this.serializedChild.onNext(value);
                                this.arbiter.produced(1L);
                            }
                            synchronized (this.guard) {
                                localQueue = this.queue;
                                this.queue = null;
                                if (localQueue == null) {
                                    break;
                                } else if (this.serializedChild.isUnsubscribed()) {
                                    break;
                                }
                            }
                        } catch (Throwable th) {
                            if (!skipFinal) {
                                synchronized (this.guard) {
                                    this.emitting = false;
                                }
                            }
                            throw th;
                        }
                    }
                    if (!skipFinal) {
                        synchronized (this.guard) {
                            this.emitting = false;
                        }
                    }
                }
            }
        }

        void drain(List<Object> list) {
            if (list != null) {
                for (Object obj : list) {
                    if (this.nl.isCompleted(obj)) {
                        this.serializedChild.onCompleted();
                        return;
                    } else if (this.nl.isError(obj)) {
                        this.serializedChild.onError(this.nl.getError(obj));
                        return;
                    } else {
                        this.serializedChild.onNext((T) obj);
                        this.arbiter.produced(1L);
                    }
                }
            }
        }

        void error(Throwable e, int id) {
            synchronized (this.guard) {
                if (id == this.index) {
                    if (this.emitting) {
                        if (this.queue == null) {
                            this.queue = new ArrayList();
                        }
                        this.queue.add(this.nl.error(e));
                    } else {
                        List<Object> localQueue = this.queue;
                        this.queue = null;
                        this.emitting = true;
                        drain(localQueue);
                        this.serializedChild.onError(e);
                        unsubscribe();
                    }
                }
            }
        }

        void complete(int id) {
            synchronized (this.guard) {
                if (id == this.index) {
                    this.active = false;
                    if (this.mainDone) {
                        if (this.emitting) {
                            if (this.queue == null) {
                                this.queue = new ArrayList();
                            }
                            this.queue.add(this.nl.completed());
                        } else {
                            List<Object> localQueue = this.queue;
                            this.queue = null;
                            this.emitting = true;
                            drain(localQueue);
                            this.serializedChild.onCompleted();
                            unsubscribe();
                        }
                    }
                }
            }
        }
    }

    private static final class InnerSubscriber<T> extends Subscriber<T> {
        private final ProducerArbiter arbiter;
        private final int id;
        private final SwitchSubscriber<T> parent;

        InnerSubscriber(int id, ProducerArbiter arbiter, SwitchSubscriber<T> parent) {
            this.id = id;
            this.arbiter = arbiter;
            this.parent = parent;
        }

        @Override // rx.Subscriber
        public void setProducer(Producer p) {
            this.arbiter.setProducer(p);
        }

        @Override // rx.Observer
        public void onNext(T t) {
            this.parent.emit(t, this.id, this);
        }

        @Override // rx.Observer
        public void onError(Throwable e) {
            this.parent.error(e, this.id);
        }

        @Override // rx.Observer
        public void onCompleted() {
            this.parent.complete(this.id);
        }
    }
}
