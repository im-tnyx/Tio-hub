package com.tnyx.wear.device.listener

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompanionDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/health_summary") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val calories = dataMap.getInt("calories")
                    val waterCups = dataMap.getInt("water")
                    val steps = dataMap.getInt("steps")
                    
                    serviceScope.launch {
                        // TODO: Save to local Room DB or SharedPreferences
                    }
                }
            }
        }
    }
}
