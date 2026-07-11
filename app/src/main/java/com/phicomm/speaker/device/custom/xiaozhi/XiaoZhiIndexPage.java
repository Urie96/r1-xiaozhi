package com.phicomm.speaker.device.custom.xiaozhi;

final class XiaoZhiIndexPage {
    private XiaoZhiIndexPage() {
    }

    static byte[] bytes() throws Exception {
        return html().getBytes("UTF-8");
    }

    private static String html() {
        return "<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>斐讯 R1 控制台</title><style>"
                + ":root{color-scheme:light dark;--bg:#f4f6f8;--card:#fff;--text:#17212b;--muted:#687684;--line:#d9e0e6;--accent:#1677ff;--danger:#d4380d}"
                + "@media(prefers-color-scheme:dark){:root{--bg:#101418;--card:#1a2026;--text:#edf2f7;--muted:#9aa8b5;--line:#34404a;--accent:#4096ff}}"
                + "*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:15px/1.5 system-ui,-apple-system,sans-serif}"
                + "main{max-width:850px;margin:auto;padding:20px}h1{margin:0 0 4px;font-size:25px}h2{margin:0 0 14px;font-size:18px}.sub{color:var(--muted);margin-bottom:18px}"
                + ".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:14px}.card{background:var(--card);border:1px solid var(--line);border-radius:12px;padding:17px}"
                + ".wide{grid-column:1/-1}.row{display:flex;gap:9px;align-items:center;flex-wrap:wrap;margin:9px 0}.field{display:grid;grid-template-columns:1fr 150px;gap:12px;align-items:center;margin:10px 0}"
                + "input[type=text],input[type=number]{width:100%;padding:8px 10px;border:1px solid var(--line);border-radius:7px;background:var(--bg);color:var(--text)}"
                + "button{border:0;border-radius:7px;padding:9px 13px;background:var(--accent);color:white;cursor:pointer}button.alt{background:#596773}button.danger{background:var(--danger)}"
                + ".status{display:grid;grid-template-columns:auto 1fr;gap:5px 12px}.status span:nth-child(odd){color:var(--muted)}#message{min-height:24px;margin:10px 0;color:var(--muted)}"
                + ".warn{color:#d48806}code{overflow-wrap:anywhere}@media(max-width:520px){.field{grid-template-columns:1fr}.field input{margin-top:-5px}}"
                + "</style></head><body><main><h1>斐讯 R1 控制台</h1><div class=\"sub\">查看状态、修改小智配置并控制音箱</div>"
                + "<div id=\"message\">正在读取状态…</div><div class=\"grid\">"
                + "<section class=\"card wide\"><h2>小智配置</h2><form id=\"configForm\">"
                + "<label class=\"field\"><span>VAD 前静音（毫秒）</span><input id=\"vadFront\" type=\"number\" min=\"1000\" max=\"120000\" required></label>"
                + "<label class=\"field\"><span>VAD 后静音（毫秒）</span><input id=\"vadBack\" type=\"number\" min=\"100\" max=\"5000\" required></label>"
                + "<label class=\"field\"><span>WebSocket 地址</span><input id=\"wsUrl\" type=\"text\" required></label>"
                + "<label class=\"field\"><span>多轮进入 ASR 延迟（毫秒）</span><input id=\"asrDelay\" type=\"number\" min=\"0\" max=\"10000\" required></label>"
                + "<label class=\"row\"><input id=\"multiTurn\" type=\"checkbox\"> 启用多轮交互</label>"
                + "<div class=\"row\"><button type=\"submit\">保存配置</button><span id=\"restartHint\"></span></div></form></section>"
                + "<section class=\"card\"><h2>音量</h2><div class=\"status\"><span>当前</span><strong id=\"volumeText\">-</strong></div>"
                + "<div class=\"row\"><button data-post=\"/api/volume/down\">降低</button><button data-post=\"/api/volume/up\">提高</button><button data-post=\"/api/volume/min\" class=\"alt\">最小</button><button data-post=\"/api/volume/max\" class=\"alt\">最大</button></div>"
                + "<form id=\"volumeForm\" class=\"row\"><input id=\"volumePercent\" type=\"number\" min=\"0\" max=\"100\" placeholder=\"百分比\" required><button>设置</button></form></section>"
                + "<section class=\"card\"><h2>蓝牙</h2><div class=\"status\"><span>适配器</span><strong id=\"btAdapter\">-</strong><span>蓝牙模式</span><strong id=\"btMode\">-</strong></div>"
                + "<div class=\"row\"><button data-post=\"/api/bluetooth/on\">开启</button><button data-post=\"/api/bluetooth/off\" class=\"alt\">关闭</button></div></section>"
                + "<section class=\"card\"><h2>语音引擎</h2><div class=\"status\"><span>引擎状态</span><strong id=\"engineState\">-</strong><span>唤醒</span><strong id=\"wakeupState\">-</strong><span>ASR</span><strong id=\"asrState\">-</strong><span>TTS</span><strong id=\"ttsState\">-</strong></div>"
                + "<div class=\"row\"><button data-post=\"/api/asr/wakeup\">进入 ASR</button></div></section>"
                + "<section class=\"card\"><h2>休眠</h2><div class=\"status\"><span>状态</span><strong id=\"sleepState\">-</strong></div>"
                + "<div class=\"row\"><button data-post=\"/api/sleep/start\" class=\"danger\">开始休眠</button><button data-post=\"/api/sleep/end\">结束休眠</button></div></section>"
                + "<section class=\"card wide\"><div class=\"row\"><button id=\"refresh\" type=\"button\">刷新状态</button><span>状态接口：<code>GET /api/status</code></span></div></section>"
                + "</div></main><script>"
                + "var $=function(id){return document.getElementById(id)};function yn(v){return v?'是':'否'};function message(s,bad){$('message').textContent=s;$('message').style.color=bad?'#d4380d':''}"
                + "function request(url,opt){return fetch(url,opt).then(function(r){return r.json().then(function(j){if(!r.ok||j.ok===false)throw new Error(j.error||('HTTP '+r.status));return j})})}"
                + "function render(s){var c=s.config,b=s.bluetooth,v=s.volume,a=s.asr,p=s.sleep;$('vadFront').value=c.vadFrontSilenceMs;$('vadBack').value=c.vadBackSilenceMs;$('wsUrl').value=c.wsUrl;$('asrDelay').value=c.enterAsrDelayMs;$('multiTurn').checked=c.multiTurnEnabled;"
                + "$('restartHint').textContent=c.restartRequired?'需要重启音箱应用后生效':'';$('restartHint').className=c.restartRequired?'warn':'';$('volumeText').textContent=v.current+' / '+v.max+'（'+v.percent+'%）';$('btAdapter').textContent=vBool(b.adapterEnabled);$('btMode').textContent=vBool(b.bluetoothMode);"
                + "$('engineState').textContent=a.contextReady?a.engineState:'未就绪';$('wakeupState').textContent=vBool(a.wakeup);$('asrState').textContent=vBool(a.asr);$('ttsState').textContent=vBool(a.ttsPlaying);$('sleepState').textContent=p.sleeping?'休眠':'唤醒';message('状态已更新',false)}"
                + "function vBool(v){return typeof v==='boolean'?yn(v):'-'}function refresh(){message('正在读取状态…',false);return request('/api/status').then(render).catch(function(e){message(e.message,true)})}"
                + "function post(url,body){var opt={method:'POST'};if(body){opt.headers={'Content-Type':'application/json'};opt.body=JSON.stringify(body)}message('正在执行…',false);return request(url,opt).then(function(){message('操作成功，正在等待状态更新…',false);return new Promise(function(resolve){setTimeout(resolve,500)}).then(refresh)}).catch(function(e){message(e.message,true)})}"
                + "document.querySelectorAll('[data-post]').forEach(function(b){b.onclick=function(){post(b.getAttribute('data-post'))}});$('refresh').onclick=refresh;"
                + "$('configForm').onsubmit=function(e){e.preventDefault();post('/api/config',{vadFrontSilenceMs:Number($('vadFront').value),vadBackSilenceMs:Number($('vadBack').value),wsUrl:$('wsUrl').value,enterAsrDelayMs:Number($('asrDelay').value),multiTurnEnabled:$('multiTurn').checked})};"
                + "$('volumeForm').onsubmit=function(e){e.preventDefault();post('/api/volume/set',{percent:Number($('volumePercent').value)})};refresh();"
                + "</script></body></html>";
    }
}
