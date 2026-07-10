package org.eclipse.paho.client.mqttv3.a;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* JADX INFO: loaded from: classes.dex */
public class g implements Runnable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    static Class f492a;
    private static final String b;
    private static final Logger c;
    private e f;
    private a g;
    private org.eclipse.paho.client.mqttv3.a.b.f h;
    private i i;
    private volatile boolean k;
    private boolean d = false;
    private Object e = new Object();
    private Thread j = null;

    static {
        Class<?> cls = f492a;
        if (cls == null) {
            try {
                cls = Class.forName("org.eclipse.paho.client.mqttv3.a.g");
                f492a = cls;
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
        b = cls.getName();
        c = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, b);
    }

    public g(a aVar, e eVar, i iVar, InputStream inputStream) {
        this.f = null;
        this.g = null;
        this.i = null;
        this.h = new org.eclipse.paho.client.mqttv3.a.b.f(eVar, inputStream);
        this.g = aVar;
        this.f = eVar;
        this.i = iVar;
        c.setResourceName(aVar.k().getClientId());
    }

    public void a() {
        synchronized (this.e) {
            c.fine(b, "stop", "850");
            if (this.d) {
                this.d = false;
                this.k = false;
                if (!Thread.currentThread().equals(this.j)) {
                    try {
                        this.j.join();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        this.j = null;
        c.fine(b, "stop", "851");
    }

    public void a(String str) {
        c.fine(b, "start", "855");
        synchronized (this.e) {
            if (!this.d) {
                this.d = true;
                this.j = new Thread(this, str);
                this.j.start();
            }
        }
    }

    public boolean b() {
        return this.d;
    }

    public boolean c() {
        return this.k;
    }

    /* JADX WARN: Code restructure failed: missing block: B:7:0x000b, code lost:
    
        org.eclipse.paho.client.mqttv3.a.g.c.fine(org.eclipse.paho.client.mqttv3.a.g.b, "run", "854");
     */
    /* JADX WARN: Code restructure failed: missing block: B:8:0x0016, code lost:
    
        return;
     */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        MqttToken mqttToken = null;
        while (true) {
            MqttToken mqttToken2 = mqttToken;
            if (!this.d || this.h == null) {
                break;
            }
            try {
                try {
                    try {
                        c.fine(b, "run", "852");
                        this.k = this.h.available() > 0;
                        org.eclipse.paho.client.mqttv3.a.b.u uVarA = this.h.a();
                        this.k = false;
                        if (uVarA instanceof org.eclipse.paho.client.mqttv3.a.b.b) {
                            MqttToken mqttTokenA = this.i.a(uVarA);
                            if (mqttTokenA == null) {
                                throw new MqttException(6);
                            }
                            synchronized (mqttTokenA) {
                                this.f.a((org.eclipse.paho.client.mqttv3.a.b.b) uVarA);
                            }
                            mqttToken = mqttTokenA;
                        } else {
                            this.f.b(uVarA);
                            mqttToken = mqttToken2;
                        }
                    } catch (IOException e) {
                        c.fine(b, "run", "853");
                        this.d = false;
                        if (this.g.f()) {
                            mqttToken = mqttToken2;
                        } else {
                            this.g.a(mqttToken2, new MqttException(32109, e));
                            mqttToken = mqttToken2;
                        }
                    }
                } catch (MqttException e2) {
                    c.fine(b, "run", "856", null, e2);
                    this.d = false;
                    this.g.a(mqttToken2, e2);
                    mqttToken = mqttToken2;
                }
            } finally {
                this.k = false;
            }
        }
    }
}
