/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.bestgif

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.android.bestgif.networking.GiphyApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_gif.*
import java.util.concurrent.TimeUnit

class GifActivity : AppCompatActivity() {

    private val adapter = GiphyAdapter()
    private val disposables = CompositeDisposable()
    private val locationRequestCode = 500
    private val permissionsSubject = BehaviorSubject.create<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        text_input
            .textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .flatMap { getGifs(it) }
            .onErrorReturnItem(listOf(GiphyGif("https://media.giphy.com/media/SQ24FpNRW9yRG/giphy.gif")))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.items = it }
            .addTo(disposables)

        permissionsSubject.doOnSubscribe {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationRequestCode
                )
            } else {
                permissionsSubject.onNext(true)
            }
        }
            .filter { it }
            .flatMap { locationUpdates(this) }
            .take(1)
            .map { cityFromLocation(this, it) }
            .doOnNext { text_input.hint = it }
            .flatMap { GiphyApi.searchForGifs(it).subscribeOn(Schedulers.io()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.items = it }
            .addTo(disposables)
    }

    private fun getGifs(it: String) =
        GiphyApi
            .searchForGifs(it)
            .subscribeOn(Schedulers.io())

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions:
        Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            val locationIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (locationIndex != -1) {
                val granted = grantResults[locationIndex] == PackageManager.PERMISSION_GRANTED
                permissionsSubject.onNext(granted)
            }
        }
    }
}
