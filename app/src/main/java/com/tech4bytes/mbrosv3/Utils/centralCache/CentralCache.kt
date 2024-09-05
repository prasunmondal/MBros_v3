package com.tech4bytes.extrack.centralCache

import android.content.Context
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.extrack.centralCache.utils.CacheUtils
import com.tech4bytes.extrack.centralCache.utils.ClassDetailsUtils
import com.tech4bytes.mbrosv3.Utils.Files.IOObjectToFile
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.centralCache.CacheFilesList
import com.tech4bytes.mbrosv3.Utils.centralCache.CacheModel
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class CentralCache {

    // Map of filenames, and (contents with key)
    var cache: MutableMap<String, MutableMap<String, CacheModel>> = hashMapOf()

    private fun saveCacheDataToFile(cacheKey: String) {
        LogMe.log("Saving cache data - File: ${getFileName(cacheKey)}")
        val filename = getFileName(cacheKey)
        val writeObj = IOObjectToFile()
        writeObj.WriteObjectToFile(AppContexts.get(), filename, cache)
        CacheFilesList.addToCacheFilesList(filename)
    }

    private fun getCacheDataFromFile(context: Context, cacheKey: String): MutableMap<String, MutableMap<String, CacheModel>> {
        return try {
            val readObj = IOObjectToFile()
            val result = readObj.ReadObjectFromFile(
                context,
                getFileName(cacheKey)
            ) as MutableMap<String, MutableMap<String, CacheModel>>
            LogMe.log("Reading records from file: ${getFileName(cacheKey)}: Successful")
            result
        } catch (e: Exception) {
            LogMe.log("Reading records from file: ${getFileName(cacheKey)}: Failed")
            mutableMapOf()
        }
    }

    companion object {

        private var centralCache = CentralCache()

        private fun getFileName(cacheKey: String): String {
            return "CentralCache-" + if (Configuration.configs.storagePatternType == Configuration.DATA_STORING_TYPE.CLASS_FILES) {
                CacheUtils.getClassKey()
            } else if (Configuration.configs.storagePatternType == Configuration.DATA_STORING_TYPE.CACHE_KEY) {
                cacheKey.replace("/","-")
            }
            else {
                "data.dat"
            }
        }

        fun <T> get(context: Context, key: String, useCache: Boolean = true): T? {


            // if user wants to force refresh the values in the cache, pass useCache as false
            if (!useCache) {
                LogMe.log("UseCache: False (Forced to not use cached data)")
                return null
            }

            val cacheObjectKey = CacheUtils.getCacheKey(key)
            val cacheClassKey = CacheUtils.getClassKey()
            LogMe.log("Getting data from Cache - key: $cacheObjectKey")
            var classElements = centralCache.cache[cacheClassKey]

            if (classElements != null && classElements.containsKey(cacheObjectKey)) {
                LogMe.log("Cache Hit (key:$cacheObjectKey)- Local Memory")
                val cacheObj = classElements[cacheObjectKey]!!
                if (cacheObj.expiryTime.isBefore(LocalDateTime.now())) {
                    LogMe.log("Data Expired (key:$cacheObjectKey)")
                    LogMe.log("Deleting cache data")
                    centralCache.cache[cacheClassKey]!!.remove(cacheObjectKey)
                    centralCache.saveCacheDataToFile(cacheObjectKey)
                    return null
                }
                return cacheObj.content as T
            }

            centralCache.cache = centralCache.getCacheDataFromFile(context, cacheObjectKey)
            classElements = centralCache.cache[cacheClassKey]
            if (classElements != null && classElements.containsKey(cacheObjectKey)) {
                LogMe.log("Cache Hit (key:$cacheObjectKey)- File")
                val cacheObj = classElements[cacheObjectKey]!!
                if (cacheObj.expiryTime.isBefore(LocalDateTime.now())) {
                    LogMe.log("Data Expired (key:$cacheObjectKey)- Local Memory")
                    LogMe.log("Deleting cache data")
                    centralCache.cache[cacheClassKey]!!.remove(cacheObjectKey)
                    centralCache.saveCacheDataToFile(cacheObjectKey)
                    return null
                }
                return cacheObj.content as T
            }

            LogMe.log("Cache Miss (key:$cacheObjectKey)")
            return null
        }

        /**
         * First try to get the value from cache,
         * if not available,
         * prepares the data, saves to cache for future use and returns it
         */
//        fun <T: Any> getOrPutNGet(context: Context, key: String, contentGenerator: Consumer<T>): T {
//            val isCacheHit = get<T>(context, key)
//            if(isCacheHit == null) {
//                var content = contentGenerator.accept() as T
//                put(key, content)
//            }
//            return content
//        }

        fun <T> put(key: String, data: T) {
            val cacheClassKey = CacheUtils.getClassKey()
            val cacheKey = CacheUtils.getCacheKey(key)
            LogMe.log("Putting data to Cache - key: $cacheKey")
            val presentData = centralCache.cache[cacheClassKey]
            if (presentData == null) {
                centralCache.cache[cacheClassKey] = hashMapOf()
            }
            centralCache.cache[cacheClassKey]!![cacheKey] = CacheModel(data as Any?)
            centralCache.saveCacheDataToFile(cacheKey)
        }

        fun <T> putNGet(key: String, data: T): T {
            put(key, data)
            return data
        }

        fun invalidateFullCache() {
            centralCache.cache.clear()
            val cacheFiles = CacheFilesList.getCacheFilesList()
            val writeObj = IOObjectToFile()
            cacheFiles.forEach {
                LogMe.log("Clearing cache: deleting file - $it")
                writeObj.WriteObjectToFile(AppContexts.get(), it, null)
            }
            CacheFilesList.clearCacheFilesList()
        }

        fun invalidateClassCache(cacheKey: String) {
            centralCache.cache[CacheUtils.getClassKey()] = hashMapOf()
            centralCache.saveCacheDataToFile(cacheKey)
        }

        fun <T : Any> invalidateClassCache(clazz: KClass<T>, cacheKey: String) {
            centralCache.cache[ClassDetailsUtils.getClassName(clazz)] = hashMapOf()
            centralCache.saveCacheDataToFile(cacheKey)
        }
    }
}