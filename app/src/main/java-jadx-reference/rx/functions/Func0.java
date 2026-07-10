package rx.functions;

import java.util.concurrent.Callable;

/* JADX INFO: loaded from: classes.dex */
public interface Func0<R> extends Function, Callable<R> {
    R call();
}
