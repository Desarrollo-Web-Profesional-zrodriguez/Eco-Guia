package mx.utng.ecoguiawear.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.utng.ecoguia.shared.data.EcoGuiaDatabase

class SyncOfflineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = EcoGuiaDatabase.getDatabase(applicationContext)
        val dao = database.dao()
        val pendingActions = dao.getAllPendingSyncActions()

        if (pendingActions.isEmpty()) {
            return Result.success()
        }

        for (action in pendingActions) {
            try {
                when (action.actionType) {
                    "SAVE_ROUTE" -> {
                        val json = org.json.JSONObject(action.payloadJson)
                        val userId = json.getString("user_id")
                        val routeId = json.getString("route_id")
                        mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl(context = applicationContext).saveRouteToCollection(userId, routeId)
                    }
                    "MARK_VISITED" -> {
                        val json = org.json.JSONObject(action.payloadJson)
                        val dropId = json.getString("geo_drop_id")
                        dao.markAsVisited(dropId)
                    }
                    "SAVE_GEODROP" -> {
                        val json = org.json.JSONObject(action.payloadJson)
                        val userId = json.getString("user_id")
                        val geoDropId = json.getString("geo_drop_id")
                        mx.utng.ecoguia.shared.data.repository.EcoGuiaRepositoryImpl(context = applicationContext).saveGeoDropToCollection(userId, geoDropId, null)
                    }
                }
                dao.deletePendingSyncAction(action.id)
            } catch (e: Exception) {
                // Reintenta en el siguiente ciclo si falla la red
            }
        }

        return Result.success()
    }

    companion object {
        fun enqueueSync(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncOfflineWorker>()
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "EcoGuiaSyncWorker",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
        }
    }
}
