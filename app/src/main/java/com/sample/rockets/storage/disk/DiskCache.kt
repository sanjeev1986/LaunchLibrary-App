package com.sample.rockets.storage.disk

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Since the API doesn's support HTTP cacheing, create one with this
 */
class DiskCache(private val application: Application) {
    /**
     * Serialize and store a json object in file
     */
    fun <T> saveFile(name: String, data: T): Completable = Completable.fromAction {
        var bout: BufferedOutputStream? = null
        try {
            val fileDir = File(application.filesDir.absolutePath + File.separator + name)
            if (fileDir.exists()) {
                application.deleteFile(name)
            }
            val gson = Gson()
            val str = gson.toJson(data)

            val fout = application.openFileOutput(name, Context.MODE_PRIVATE)
            bout = BufferedOutputStream(fout)
            bout.write(str.toByteArray())

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bout?.flush()
            bout?.close()
        }

    }.compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Deserialize a json object from file if it exists
     */
    fun <T> readFile(name: String, cls: Class<T>): Maybe<T> {

        return Maybe.create<T> { subscriber ->
            var bin: BufferedReader? = null
            try {
                val fileDir = File(application.filesDir.absolutePath + File.separator + name)
                if (fileDir.exists()) {
                    val fin = application.openFileInput(name)
                    bin = BufferedReader(InputStreamReader(fin))
                    val gson = Gson()
                    val d = gson.fromJson(bin, cls)
                    bin.close()
                    subscriber.onSuccess(d)
                }
                subscriber.onComplete()
            } catch (e: java.lang.Exception) {
                subscriber.onError(e)
            } finally {
                bin?.close()
            }

        }.compose {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

    }

    fun deleteCache(name: String): Completable {
        return Completable.fromAction {
            try {
                val fileDir = File(application.filesDir.absolutePath + File.separator + name)
                fileDir.delete()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }.compose {
            it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }


    }
}