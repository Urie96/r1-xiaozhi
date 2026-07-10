package rx.internal.util.unsafe;

/* JADX INFO: compiled from: SpscUnboundedArrayQueue.java */
/* JADX INFO: loaded from: classes.dex */
abstract class SpscUnboundedArrayQueueProducerColdFields<E> extends SpscUnboundedArrayQueueProducerFields<E> {
    protected E[] producerBuffer;
    protected long producerLookAhead;
    protected int producerLookAheadStep;
    protected long producerMask;

    SpscUnboundedArrayQueueProducerColdFields() {
    }
}
