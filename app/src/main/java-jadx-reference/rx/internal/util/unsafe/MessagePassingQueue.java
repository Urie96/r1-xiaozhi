package rx.internal.util.unsafe;

/* JADX INFO: loaded from: classes.dex */
interface MessagePassingQueue<M> {
    boolean isEmpty();

    boolean offer(M m);

    M peek();

    M poll();

    int size();
}
