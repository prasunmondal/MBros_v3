package com.tech4bytes.mbrosv3.Utils.centralCache

import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Files.IOObjectToFile
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class CacheFilesList : java.io.Serializable {
    companion object {

        private val cacheFilesFileName: String = "cacheFilesIndex.dat"

        fun getCacheFilesList(): MutableList<String> {
            val readObj = IOObjectToFile()
            val list = try {
                readObj.ReadObjectFromFile(
                    AppContexts.get(),
                    cacheFilesFileName
                ) as MutableList<String>
            } catch (e: Exception) {
                LogMe.log(e)
                mutableListOf()
            }
            return list
        }

        fun addToCacheFilesList(classKey: String) {
            val list = getCacheFilesList()
            if (list.contains(classKey))
                return
            list.add("CentralCache:$classKey")
            val writeObj = IOObjectToFile()
            writeObj.WriteObjectToFile(AppContexts.get(), cacheFilesFileName, list)
        }

        fun removeFromCacheFilesList(classKey: String) {
            val list = getCacheFilesList()
            if (!list.contains(classKey))
                return
            list.remove(classKey)
            val writeObj = IOObjectToFile()
            writeObj.WriteObjectToFile(AppContexts.get(), cacheFilesFileName, list)
        }
    }
}