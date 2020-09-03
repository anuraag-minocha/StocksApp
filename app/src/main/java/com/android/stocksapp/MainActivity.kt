package com.android.stocksapp


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var webSocketClient: WebSocketClient
    val list = ArrayList<Stock>()
    val map = HashMap<String, Int>()
    lateinit var stockRecyclerAdapter: StockRecyclerAdapter
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "stock-list.db"
        ).build()
        stockRecyclerAdapter = StockRecyclerAdapter(arrayListOf())
        recyclerView.adapter = stockRecyclerAdapter
        lifecycleScope.launch {
            val dataList = db.stockDao().getAll()
            if (dataList.isNotEmpty()) {
                for (stock in dataList) {
                    list.add(stock)
                    map.put(stock.name, list.size - 1)
                }
            }
            stockRecyclerAdapter.updateList(list)
            createWebSocketClient()
        }
    }

    override fun onResume() {
        super.onResume()
        stockRecyclerAdapter.notifyDataSetChanged()
    }

    private fun createWebSocketClient() {
        val uri: URI
        try {
            uri = URI("ws://stocks.mnet.website/")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                Log.i("WebSocket", "Session is starting")
//                webSocketClient.send("Hello World!")
            }

            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received: $s")
                val jsonArray = JSONArray(s)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONArray(i)
                    if (map.containsKey(item.optString(0))) {
                        val position = map[item.optString(0)]!!
                        val stock = list[position]
                        stock.increase = if (stock.price < item.optDouble(1)) 1 else -1
                        stock.price = item.optDouble(1)
                        stock.lastUpdate = Calendar.getInstance().timeInMillis
                        runOnUiThread {
                            stockRecyclerAdapter.changeItem(stock, position)
                        }
                        GlobalScope.launch {
                            db.stockDao()
                                .updateStock(stock)
                        }
                    } else {
                        val stock = Stock(
                            item.optString(0),
                            item.optDouble(1),
                            Calendar.getInstance().timeInMillis,
                            0
                        )
                        list.add(stock)
                        map.put(stock.name, list.size - 1)
                        runOnUiThread {
                            stockRecyclerAdapter.addItem(stock)
                        }
                        GlobalScope.launch {
                            db.stockDao().insertAll(stock)
                        }
                    }
                }
            }

            override fun onBinaryReceived(data: ByteArray) {}
            override fun onPingReceived(data: ByteArray) {}
            override fun onPongReceived(data: ByteArray) {}
            override fun onException(e: Exception) {
                println(e.message)
            }

            override fun onCloseReceived() {
                Log.i("WebSocket", "Closed ")
                println("onCloseReceived")
            }
        }
        webSocketClient.setConnectTimeout(10000)
        webSocketClient.setReadTimeout(60000)
        webSocketClient.enableAutomaticReconnection(5000)
        webSocketClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}