package rx.internal.util.unsafe;

import java.util.AbstractQueue;

/* JADX INFO: compiled from: SpscUnboundedArrayQueue.java */
/* JADX INFO: loaded from: classes.dex */
abstract class SpscUnboundedArrayQueueProducerFields<E> extends AbstractQueue<E> {
    protected long producerIndex;

    SpscUnboundedArrayQueueProducerFields() {
    }
}
