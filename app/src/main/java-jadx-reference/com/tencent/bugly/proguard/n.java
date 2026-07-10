package com.tencent.bugly.proguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class n {
    private Context c;
    private SharedPreferences f;
    private static n b = null;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static final long f194a = System.currentTimeMillis();
    private Map<Integer, Map<String, m>> e = new HashMap();
    private String d = com.tencent.bugly.crashreport.common.info.a.b().d;

    private n(Context context) {
        this.c = context;
        this.f = context.getSharedPreferences("crashrecord", 0);
    }

    public static synchronized n a(Context context) {
        if (b == null) {
            b = new n(context);
        }
        return b;
    }

    public static synchronized n a() {
        return b;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized boolean b(int i) {
        boolean z;
        try {
            List<m> listC = c(i);
            if (listC == null) {
                z = false;
            } else {
                long jCurrentTimeMillis = System.currentTimeMillis();
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                for (m mVar : listC) {
                    if (mVar.b != null && mVar.b.equalsIgnoreCase(this.d) && mVar.d > 0) {
                        arrayList.add(mVar);
                    }
                    if (mVar.c + 86400000 < jCurrentTimeMillis) {
                        arrayList2.add(mVar);
                    }
                }
                Collections.sort(arrayList);
                if (arrayList.size() >= 2) {
                    if (arrayList.size() > 0 && ((m) arrayList.get(arrayList.size() - 1)).c + 86400000 < jCurrentTimeMillis) {
                        listC.clear();
                        a(i, listC);
                        z = false;
                    } else {
                        z = true;
                    }
                } else {
                    listC.removeAll(arrayList2);
                    a(i, listC);
                    z = false;
                }
            }
        } catch (Exception e) {
            x.e("isFrequentCrash failed", new Object[0]);
            z = false;
        }
        return z;
    }

    public final synchronized void a(int i, final int i2) {
        final int i3 = 1004;
        w.a().a(new Runnable() { // from class: com.tencent.bugly.proguard.n.1
            @Override // java.lang.Runnable
            public final void run() {
                m mVar;
                try {
                    if (!TextUtils.isEmpty(n.this.d)) {
                        List listC = n.this.c(i3);
                        List<m> arrayList = listC == null ? new ArrayList() : listC;
                        if (n.this.e.get(Integer.valueOf(i3)) == null) {
                            n.this.e.put(Integer.valueOf(i3), new HashMap());
                        }
                        if (((Map) n.this.e.get(Integer.valueOf(i3))).get(n.this.d) != null) {
                            m mVar2 = (m) ((Map) n.this.e.get(Integer.valueOf(i3))).get(n.this.d);
                            mVar2.d = i2;
                            mVar = mVar2;
                        } else {
                            m mVar3 = new m();
                            mVar3.f193a = i3;
                            mVar3.g = n.f194a;
                            mVar3.b = n.this.d;
                            mVar3.f = com.tencent.bugly.crashreport.common.info.a.b().j;
                            com.tencent.bugly.crashreport.common.info.a.b().getClass();
                            mVar3.e = "2.6.6";
                            mVar3.c = System.currentTimeMillis();
                            mVar3.d = i2;
                            ((Map) n.this.e.get(Integer.valueOf(i3))).put(n.this.d, mVar3);
                            mVar = mVar3;
                        }
                        ArrayList arrayList2 = new ArrayList();
                        boolean z = false;
                        for (m mVar4 : arrayList) {
                            if (mVar4.g == mVar.g && mVar4.b != null && mVar4.b.equalsIgnoreCase(mVar.b)) {
                                z = true;
                                mVar4.d = mVar.d;
                            }
                            if ((mVar4.e != null && !mVar4.e.equalsIgnoreCase(mVar.e)) || ((mVar4.f != null && !mVar4.f.equalsIgnoreCase(mVar.f)) || mVar4.d <= 0)) {
                                arrayList2.add(mVar4);
                            }
                        }
                        arrayList.removeAll(arrayList2);
                        if (!z) {
                            arrayList.add(mVar);
                        }
                        n.this.a(i3, arrayList);
                    }
                } catch (Exception e) {
                    x.e("saveCrashRecord failed", new Object[0]);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:33:0x006c A[Catch: Exception -> 0x003a, all -> 0x0055, TryCatch #7 {Exception -> 0x003a, blocks: (B:4:0x0002, B:11:0x0036, B:33:0x006c, B:34:0x006f, B:29:0x0064, B:20:0x0051), top: B:46:0x0002, outer: #6 }] */
    /* JADX WARN: Type inference failed for: r2v10 */
    /* JADX WARN: Type inference failed for: r2v4, types: [boolean] */
    /* JADX WARN: Type inference failed for: r2v5, types: [java.io.ObjectInputStream] */
    /* JADX WARN: Type inference failed for: r2v6 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public synchronized <T extends List<?>> T c(int i) {
        T t;
        ObjectInputStream objectInputStream;
        ObjectInputStream objectInputStream2;
        try {
            File file = new File(this.c.getDir("crashrecord", 0), new StringBuilder().append(i).toString());
            ?? Exists = file.exists();
            try {
                if (Exists == 0) {
                    t = null;
                } else {
                    try {
                        objectInputStream = new ObjectInputStream(new FileInputStream(file));
                        try {
                            t = (T) objectInputStream.readObject();
                            objectInputStream.close();
                        } catch (IOException e) {
                            objectInputStream2 = objectInputStream;
                            try {
                                x.a("open record file error", new Object[0]);
                                if (objectInputStream2 != null) {
                                    objectInputStream2.close();
                                }
                                t = null;
                            } catch (Throwable th) {
                                Exists = objectInputStream2;
                                th = th;
                                if (Exists != 0) {
                                    Exists.close();
                                }
                                throw th;
                            }
                        } catch (ClassNotFoundException e2) {
                            x.a("get object error", new Object[0]);
                            if (objectInputStream != null) {
                                objectInputStream.close();
                            }
                            t = null;
                        }
                    } catch (IOException e3) {
                        objectInputStream2 = null;
                    } catch (ClassNotFoundException e4) {
                        objectInputStream = null;
                    } catch (Throwable th2) {
                        th = th2;
                        Exists = 0;
                        if (Exists != 0) {
                        }
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        } catch (Exception e5) {
            x.e("readCrashRecord error", new Object[0]);
        }
        return t;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0056 A[Catch: Exception -> 0x0032, all -> 0x003c, TryCatch #4 {Exception -> 0x0032, blocks: (B:6:0x0005, B:10:0x002e, B:26:0x0056, B:27:0x0059, B:22:0x004e), top: B:36:0x0005, outer: #3 }] */
    /* JADX WARN: Type inference failed for: r1v11 */
    /* JADX WARN: Type inference failed for: r1v12 */
    /* JADX WARN: Type inference failed for: r1v13 */
    /* JADX WARN: Type inference failed for: r1v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public synchronized <T extends List<?>> void a(int i, T t) {
        ObjectOutputStream objectOutputStream;
        if (t != null) {
            try {
                ObjectOutputStream dir = this.c.getDir("crashrecord", 0);
                try {
                    try {
                        objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(dir, new StringBuilder().append(i).toString())));
                        try {
                            objectOutputStream.writeObject(t);
                            objectOutputStream.close();
                            dir = objectOutputStream;
                        } catch (IOException e) {
                            e = e;
                            e.printStackTrace();
                            x.a("open record file error", new Object[0]);
                            dir = objectOutputStream;
                            if (objectOutputStream != null) {
                                objectOutputStream.close();
                                dir = objectOutputStream;
                            }
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (dir != 0) {
                            dir.close();
                        }
                        throw th;
                    }
                } catch (IOException e2) {
                    e = e2;
                    objectOutputStream = null;
                } catch (Throwable th2) {
                    th = th2;
                    dir = 0;
                    if (dir != 0) {
                    }
                    throw th;
                }
            } catch (Exception e3) {
                x.e("writeCrashRecord error", new Object[0]);
            }
        }
    }

    public final synchronized boolean a(final int i) {
        boolean z = true;
        synchronized (this) {
            try {
                z = this.f.getBoolean(i + "_" + this.d, true);
                w.a().a(new Runnable() { // from class: com.tencent.bugly.proguard.n.2
                    @Override // java.lang.Runnable
                    public final void run() {
                        n.this.f.edit().putBoolean(i + "_" + n.this.d, !n.this.b(i)).commit();
                    }
                });
            } catch (Exception e) {
                x.e("canInit error", new Object[0]);
            }
        }
        return z;
    }
}
