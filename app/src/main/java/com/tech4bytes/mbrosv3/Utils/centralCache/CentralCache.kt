package com.tech4bytes.extrack.centralCache

import android.content.Context
import com.tech4bytes.extrack.centralCache.utils.CacheUtils
import com.tech4bytes.extrack.centralCache.utils.ClassDetailsUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Files.IOObjectToFile
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.centralCache.CacheModel
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class CentralCache {

    // Map of filenames, and (contents with key)
    var cache: MutableMap<String, MutableMap<String, CacheModel>> = hashMapOf()

    private fun saveCacheDataToFile() {
        LogMe.log("Saving cache data - File: ${getFileName()} - $cache")
        val writeObj = IOObjectToFile()
        writeObj.WriteObjectToFile(AppContexts.get(), getFileName(), cache)
    }

    private fun getCacheDataFromFile(context: Context): MutableMap<String, MutableMap<String, CacheModel>> {
        LogMe.log("Reading records from file: ${getFileName()}")
        return try {
            val readObj = IOObjectToFile()
            readObj.ReadObjectFromFile(
                context,
                getFileName()
            ) as MutableMap<String, MutableMap<String, CacheModel>>
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    companion object {

        private var centralCache = CentralCache()

        private fun getFileName(): String {
            return "CentralCache:" + if(Configuration.configs.storagePatternType == Configuration.DATA_STORING_TYPE.CLASS_FILES) {
                CacheUtils.getClassKey()
            } else {
                "data.dat"
            }
        }

        fun <T> get(context: Context, key: String, useCache: Boolean = true): T? {

            // if user wants to force refresh the values in the cache, pass useCache as false
            if(!useCache) {
                LogMe.log("UseCache: False (Forced to not use cached data)")
                return null
            }

            val cacheObjectKey = CacheUtils.getCacheKey(key)
            val cacheClassKey = CacheUtils.getClassKey()
            LogMe.log("Getting data from Cache - key: $cacheObjectKey")
            var classElements = centralCache.cache[cacheClassKey]
            if (classElements != null && classElements.containsKey(cacheObjectKey)) {
                LogMe.log("Cache Hit (key:$cacheObjectKey)- Local Memory")
                return classElements[cacheObjectKey]!!.content as T
            }

            centralCache.cache = centralCache.getCacheDataFromFile(context)
            classElements = centralCache.cache[cacheClassKey]
            if (classElements != null && classElements.containsKey(cacheObjectKey)) {
                LogMe.log("Cache Hit (key:$cacheObjectKey)- File")
                return classElements[cacheObjectKey]!!.content as T
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
            val presentData = centralCache.cache[cacheClassKey]
            if(presentData == null) {
                centralCache.cache[cacheClassKey] = hashMapOf()
            }
            centralCache.cache[cacheClassKey]!![cacheKey] = CacheModel(data as Any)
            centralCache.saveCacheDataToFile()
        }

        fun <T> putNGet(key: String, data: T): T {
            put(key, data)
            return data
        }

        fun invalidateFullCache() {
            centralCache.cache = hashMapOf()
            centralCache.saveCacheDataToFile()
        }

        fun invalidateClassCache() {
            centralCache.cache[CacheUtils.getClassKey()] = hashMapOf()
            centralCache.saveCacheDataToFile()
        }

        fun <T: Any> invalidateClassCache(clazz: KClass<T>) {
            centralCache.cache[ClassDetailsUtils.getClassName(clazz)] = hashMapOf()
            centralCache.saveCacheDataToFile()
        }
    }
}