import os, json, time
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse

PAIRING_CODE=os.getenv("PAIRING_CODE","change-this-pairing-code")
app=FastAPI(title="GEBENGBOTZ Remote Support Relay")
devices={}
controllers=set()

async def broadcast_devices():
    payload={"type":"DEVICES","devices":[{"id":k,"name":v.get("name","Android"),"online":True,"lastSeen":int(time.time()),"status":v.get("status",{})} for k,v in devices.items()]}
    for ws in list(controllers):
        try: await ws.send_json(payload)
        except: controllers.discard(ws)

@app.get("/")
def home():
    return HTMLResponse(open("dashboard/index.html",encoding="utf8").read())

@app.get("/health")
def health(): return {"ok":True,"service":"GEBENGBOTZ relay"}

@app.websocket("/ws")
async def socket(ws:WebSocket):
    await ws.accept()
    role=None; device_id=None
    try:
        hello=await ws.receive_json()
        if hello.get("type")!="HELLO" or hello.get("pairingCode")!=PAIRING_CODE:
            await ws.send_json({"type":"ERROR","message":"Pairing code invalid"}); await ws.close(code=1008); return
        role=hello.get("role")
        if role=="device":
            device_id=str(hello.get("deviceId") or "")
            if not device_id or len(device_id)>80: await ws.close(code=1008); return
            devices[device_id]={"ws":ws,"name":hello.get("name","Android device"),"status":{}}
            await ws.send_json({"type":"PAIRED","deviceId":device_id})
            await broadcast_devices()
        elif role=="controller":
            controllers.add(ws); await ws.send_json({"type":"PAIRED","role":"controller"}); await broadcast_devices()
        else: await ws.close(code=1008); return
        while True:
            msg=await ws.receive_json()
            if role=="device":
                if msg.get("type")=="STATUS":
                    if device_id in devices: devices[device_id]["status"]=msg.get("status",{})
                    await broadcast_devices()
                elif msg.get("type")=="RESULT":
                    for c in list(controllers):
                        await c.send_json({"type":"RESULT","deviceId":device_id,"requestId":msg.get("requestId"),"result":msg.get("result",{})})
            else:
                if msg.get("type")=="REQUEST_STATUS":
                    target=msg.get("deviceId")
                    d=devices.get(target)
                    if d:
                        await d["ws"].send_json({"type":"REQUEST_STATUS","requestId":msg.get("requestId")})
                    else: await ws.send_json({"type":"ERROR","requestId":msg.get("requestId"),"message":"Device offline"})
    except WebSocketDisconnect: pass
    except Exception: pass
    finally:
        if role=="controller": controllers.discard(ws)
        if role=="device" and device_id and devices.get(device_id,{}).get("ws") is ws:
            devices.pop(device_id,None); await broadcast_devices()
