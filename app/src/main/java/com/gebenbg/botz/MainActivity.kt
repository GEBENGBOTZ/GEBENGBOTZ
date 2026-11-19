package com.gebenbg.botz
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.widget.*
class MainActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val layout=LinearLayout(this);layout.orientation=LinearLayout.VERTICAL;layout.setPadding(32,40,32,32)
  val title=TextView(this);title.text="GEBENGBOTZ Remote Support";title.textSize=22f;layout.addView(title)
  val url=EditText(this);url.hint="WSS server URL (wss://host/ws)";layout.addView(url)
  val code=EditText(this);code.hint="Pairing code";code.inputType=129;layout.addView(code)
  val id=EditText(this);id.hint="Device ID (unique label)";layout.addView(id)
  val note=TextView(this);note.text="Connection runs with a visible foreground notification. This app shares basic device status only.";layout.addView(note)
  val start=Button(this);start.text="Connect";layout.addView(start)
  val stop=Button(this);stop.text="Disconnect";layout.addView(stop)
  start.setOnClickListener{if(url.text.isBlank()||code.text.isBlank()||id.text.isBlank()){Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show()}else{val i=Intent(this,SupportService::class.java);i.putExtra("url",url.text.toString());i.putExtra("code",code.text.toString());i.putExtra("id",id.text.toString());startForegroundService(i);Toast.makeText(this,"Support connection started",Toast.LENGTH_SHORT).show()}}
  stop.setOnClickListener{stopService(Intent(this,SupportService::class.java))}
  setContentView(layout)
 }
}