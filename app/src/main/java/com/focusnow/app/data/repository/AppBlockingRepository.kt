package com.focusnow.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.focusnow.app.data.datastore.UserPreferencesRepository
import com.focusnow.app.data.local.BlockedAppDao
import com.focusnow.app.data.local.BlockingProfileDao
import com.focusnow.app.data.model.BlockedApp
import com.focusnow.app.data.model.BlockingProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

class AppBlockingRepository(
    private val context: Context,
    private val blockedAppDao: BlockedAppDao,
    private val profileDao: BlockingProfileDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val gson = Gson()

    val allBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getAllBlockedApps()
    val activeBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getActiveBlockedApps()
    val allProfiles: Flow<List<BlockingProfile>> = profileDao.getAllProfiles()

    fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val apps = mutableListOf<InstalledAppInfo>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue // Skip our own app

            val name = resolveInfo.loadLabel(pm).toString()
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            apps.add(InstalledAppInfo(packageName = pkg, appName = name, isSystemApp = isSystem))
        }

        return apps.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    suspend fun toggleAppBlocked(packageName: String, appName: String) {
        val apps = blockedAppDao.getAllBlockedAppsOnce()
        val existing = apps.find { it.packageName == packageName }
        if (existing != null) {
            blockedAppDao.updateBlockedApp(existing.copy(isBlocked = !existing.isBlocked))
        } else {
            blockedAppDao.insertBlockedApp(BlockedApp(packageName = packageName, appName = appName, isBlocked = true))
        }
    }

    suspend fun setAppBlockedStatus(packageName: String, appName: String, isBlocked: Boolean) {
        val apps = blockedAppDao.getAllBlockedAppsOnce()
        val existing = apps.find { it.packageName == packageName }
        if (existing != null) {
            blockedAppDao.updateBlockedApp(existing.copy(isBlocked = isBlocked))
        } else {
            blockedAppDao.insertBlockedApp(BlockedApp(packageName = packageName, appName = appName, isBlocked = isBlocked))
        }
    }

    suspend fun isAppBlocked(packageName: String): Boolean {
        val globalEnabled = userPreferencesRepository.isAppBlockingGlobalEnabled.first()
        if (!globalEnabled) return false

        // Check if active profile has this app, or fallback to active blocked apps
        val activeProfileId = userPreferencesRepository.activeBlockingProfileId.first()
        val profile = profileDao.getProfileById(activeProfileId)

        if (profile != null) {
            val list = parsePackageList(profile.blockedPackageNamesJson)
            if (list.isNotEmpty()) {
                return list.contains(packageName)
            }
        }

        val activeApps = blockedAppDao.getActiveBlockedAppsOnce()
        return activeApps.any { it.packageName == packageName }
    }

    suspend fun createDefaultProfilesIfEmpty() {
        val profiles = profileDao.getAllProfilesOnce()
        if (profiles.isEmpty()) {
            val defaultDistractions = listOf(
                "com.instagram.android",
                "com.google.android.youtube",
                "com.whatsapp",
                "com.facebook.katana",
                "com.reddit.frontpage",
                "com.twitter.android",
                "com.snapchat.android",
                "com.android.chrome"
            )

            val gateMode = BlockingProfile(
                profileName = "GATE Mode",
                description = "Blocks social media, video streaming, and games for intense GATE prep.",
                blockedPackageNamesJson = gson.toJson(defaultDistractions),
                isDefault = true
            )

            val codingMode = BlockingProfile(
                profileName = "Coding Mode",
                description = "Blocks short-form videos and social feeds during development sprints.",
                blockedPackageNamesJson = gson.toJson(listOf("com.instagram.android", "com.facebook.katana", "com.reddit.frontpage", "com.twitter.android")),
                isDefault = false
            )

            val deepFocusMode = BlockingProfile(
                profileName = "Deep Focus",
                description = "Maximum restriction profile blocking all non-essential applications.",
                blockedPackageNamesJson = gson.toJson(defaultDistractions),
                isDefault = false
            )

            profileDao.insertAllProfiles(listOf(gateMode, codingMode, deepFocusMode))
        }
    }

    suspend fun insertProfile(profile: BlockingProfile): Long =
        profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: BlockingProfile) =
        profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: BlockingProfile) =
        profileDao.deleteProfile(profile)

    fun parsePackageList(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializePackageList(list: List<String>): String =
        gson.toJson(list)
}
