package com.gebenbg.botz
import android.app.*
import android.content.*
import android.os.*
import android.provider.Settings
import android.content.pm.ServiceInfo
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
class SupportService:Service(){
 private var socket:WebSocket?=null;private var code="";private var deviceId="";private var server=""
 private val client=OkHttpClient.Builder().pingInterval(25,TimeUnit.SECONDS).build()
 override fun onBind(intent:Intent?)=null
 override fun onStartCommand(i:Intent?,flags:Int,startId:Int):Int{
  server=i?.getStringExtra("url")?:server;code=i?.getStringExtra("code")?:code;deviceId=i?.getStringExtra("id")?:deviceId
  val nm=getSystemService(NotificationManager::class.java);if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel("support","Remote support connection",NotificationManager.IMPORTANCE_LOW))
  val nb=if(Build.VERSION.SDK_INT>=26) Notification.Builder(this,"support") else Notification.Builder(this);val n=nb.setContentTitle("GEBENGBOTZ Support connected").setContentText("Status sharing active — tap Disconnect in app").setSmallIcon(android.R.drawable.stat_sys_upload).build()
  if(Build.VERSION.SDK_INT>=29)startForeground(1,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE) else startForeground(1,n)
  connect();return START_NOT_STICKY
 }
 private fun connect(){try{val req=Request.Builder().url(server).build();socket=client.newWebSocket(req,object:WebSocketListener(){
  override fun onOpen(ws:WebSocket,r:Response){ws.send(JSONObject().put("type","HELLO").put("role","device").put("pairingCode",code).put("deviceId",deviceId).put("name",android.os.Build.MODEL).toString());sendStatus(ws)}
  override fun onMessage(ws:WebSocket,text:String){try{val m=JSONObject(text);if(m.optString("type")=="REQUEST_STATUS"){sendStatus(ws);ws.send(JSONObject().put("type","RESULT").put("requestId",m.optString("requestId")).put("result",status()).toString())}}catch(_:Exception){}}
  override fun onFailure(ws:WebSocket,t:Throwable,r:Response?){stopSelf()}
 })}catch(_:Exception){stopSelf()}}
 private fun status():JSONObject{val bm=getSystemService(BATTERY_SERVICE) as android.os.BatteryManager;val pct=bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY);return JSONObject().put("battery",pct).put("model",Build.MODEL).put("android",Build.VERSION.RELEASE).put("network","connected")}
 private fun sendStatus(ws:WebSocket){ws.send(JSONObject().put("type","STATUS").put("status",status()).toString())}
 override fun onDestroy(){socket?.close(1000,"User disconnected");stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy()}
}