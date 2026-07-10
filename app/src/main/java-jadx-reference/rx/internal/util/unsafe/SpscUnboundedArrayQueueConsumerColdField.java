package rx.internal.util.unsafe;

/* JADX INFO: compiled from: SpscUnboundedArrayQueue.java */
/* JADX INFO: loaded from: classes.dex */
abstract class SpscUnboundedArrayQueueConsumerColdField<E> extends SpscUnboundedArrayQueueL2Pad<E> {
    protected E[] consumerBuffer;
    protected long consumerMask;

    SpscUnboundedArrayQueueConsumerColdField() {
    }
}
