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

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.rxjava3.core.Observable
import java.util.*


@SuppressLint("MissingPermission")
fun locationUpdates(
    context: Context,
    client: FusedLocationProviderClient = FusedLocationProviderClient(context)
): Observable<Location> {
    val currentLocationRequest =
        LocationRequest()
            .setInterval(500)
            .setFastestInterval(0)
            .setMaxWaitTime(0)
            .setSmallestDisplacement(0f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    return Observable.create { emitter ->
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result?.lastLocation?.let { emitter.onNext(it) }
            }
        }
        client.requestLocationUpdates(currentLocationRequest, callback, null)
        emitter.setCancellable {
            client.removeLocationUpdates(callback)
        }
    }
}

fun cityFromLocation(context: Context, location: Location): String {
    val gcd = Geocoder(context, Locale.getDefault())
    val addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)
    return if (addresses.size > 0) {
        addresses[0].locality
    } else {
        ""
    }
}
