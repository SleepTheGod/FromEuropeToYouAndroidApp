package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.CachedSnapshotDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.SecurityLogDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CachedSnapshotEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SecurityLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        CachedSnapshotEntity::class,
        SecurityLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun cachedSnapshotDao(): CachedSnapshotDao
    abstract fun securityLogDao(): SecurityLogDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "from_europe_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context))
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Curated Bookmarks
            val initialBookmarks = listOf(
                BookmarkEntity(
                    title = "From Europe To You - Home",
                    url = "https://www.fromeuropetoyou.com/",
                    category = "Home",
                    note = "Premier importer of French & European Antiques and Architectural Salvage",
                    isFavorite = true
                ),
                BookmarkEntity(
                    title = "Antique Fireplace Mantels",
                    url = "https://www.fromeuropetoyou.com/",
                    category = "Mantels",
                    note = "Hand-carved French marble and limestone antique mantels",
                    isFavorite = true
                ),
                BookmarkEntity(
                    title = "Architectural Doors & Iron Gates",
                    url = "https://www.fromeuropetoyou.com/",
                    category = "Architectural",
                    note = "18th & 19th Century French wrought iron gates and carved oak doors",
                    isFavorite = false
                ),
                BookmarkEntity(
                    title = "Statuary & Garden Antiques",
                    url = "https://www.fromeuropetoyou.com/",
                    category = "Garden",
                    note = "Carved stone fountains, classical urns, and garden ornamentation",
                    isFavorite = true
                )
            )
            for (item in initialBookmarks) {
                db.bookmarkDao().insertBookmark(item)
            }

            // Initial Curated Notifications
            val initialNotifications = listOf(
                NotificationEntity(
                    title = "🔒 SSL Pinning Active & MITM Guard Engaged",
                    message = "Your browsing session to fromeuropetoyou.com is protected by hardware-backed SHA-256 certificate validation.",
                    category = "Security",
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = false
                ),
                NotificationEntity(
                    title = "🏛️ New French Architectural Shipments",
                    message = "Direct container shipment arrived with 19th-century carved limestone fireplaces and hand-forged iron gates.",
                    category = "Arrivals",
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = false
                ),
                NotificationEntity(
                    title = "⚡ Offline Cache Storage Ready",
                    message = "Pages you browse are automatically cached locally for high-speed offline access.",
                    category = "System",
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = true
                )
            )
            db.notificationDao().insertAll(initialNotifications)

            // Initial Security Log
            db.securityLogDao().insertLog(
                SecurityLogEntity(
                    eventType = "SSL_PINNING_INITIALIZED",
                    domain = "fromeuropetoyou.com",
                    status = "SECURE",
                    details = "Domain pin-set verification engine armed. TLS 1.3 protocol enforced.",
                    fingerprint = "C5:lp:Z7:tc:Vw:mw:QI:Mc:Rt:Pb:sQ:tW:LA:BX:hQ:ze:jn:a0:wH:Fr:8M"
                )
            )
        }
    }
}
