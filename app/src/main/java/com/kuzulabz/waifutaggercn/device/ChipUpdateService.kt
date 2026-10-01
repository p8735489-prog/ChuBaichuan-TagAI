package com.kuzulabz.waifutaggercn.device

/** Remote chip database update hook. */
object ChipUpdateService {
    const val DATABASE_VERSION = "2026.08"
    fun needsUpdate(localVersion:String):Boolean = localVersion != DATABASE_VERSION
}
