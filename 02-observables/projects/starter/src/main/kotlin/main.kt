import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.pow
import kotlin.math.roundToInt

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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

fun main(args: Array<String>) {

    exampleOf("just") {

        // Operator just returns Observable of one item, an Integer.
        val observableJustOne = Observable.just(1)

        // Operator just returns Observable of three items and not a list of Integers.
        val observableJustMany = Observable.just(1, 2, 3)

        // Operator just returns Observable of a list of Integers.
        val list = listOf(1, 2, 3)
        val observableListIntegers = Observable.just(list)
    }

    exampleOf("iterable") {

        // Operator fromIterable creates an Observable of individual objects from a regular list of elements,
        // same as val observableJustMany = Observable.just(1, 2, 3). This is useful when you want to convert a list
        // observable sequence.
        val list = listOf(1, 2, 3)
        val observableFromIterableList = Observable.fromIterable(list)
    }

    exampleOf("subscribe") {

        // Observable wont send events until it has a subscriber, using subscribe we are listening for onNext event.
        val observable = Observable.just(1, 2, 3)
        observable.subscribe { println(it) }
    }

    exampleOf("empty") {

        // Operator empty will create empty Observable sequence with zero elements and it will just emit complete event.
        // subscribeBy is an extension function from RxKotlin and you can explicitly state what event you want to handle.
        val observable = Observable.empty<Unit>()
        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("never") {

        // Quite opposite from previous example, operator never creates an observable that does not emit anything and
        // never terminates. It can be used to represent an infinite duration.
        val observable = Observable.never<Any>()
        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("range") {

        // Operator range creates Observable from a range of values, which takes a start Integer and a count of sequential
        // Integers to generate.
        val observable = Observable.range(1, 10)

        observable.subscribe {
            val n = it.toDouble()
            val fibonacci = ((1.61803.pow(n) - 0.61803.pow(n)) / 2.23606).roundToInt()
            println(fibonacci)
        }
    }

    exampleOf("dispose") {

        // Operator just creates an Observable of Strings
        val mostPopular = Observable.just("A", "B", "C")

        // After we subscribe to the observable, we save the returned Disposable to a local variable called subscription.
        val subscription = mostPopular.subscribe { println(it) }

        // To cancel a subscription we have to call function dispose() on it, and after this the observable wont emit anymore.
        subscription.dispose()
    }

    exampleOf("composite disposable") {

        // Managing each subscription individually would be tedious. You can create a CompositeDisposable that will hold
        // all added disposables. You can dispose them all calling dispose() on the CompositeDisposable.
        val subscriptions = CompositeDisposable()

        val disposable =
            Observable
                .just("A", "B", "C")
                .subscribe {
                    println(it)
                }

        subscriptions.add(disposable)
    }

    exampleOf("create complete") {

        // Operator create creates an Observable that will specify all events on it.
        Observable.create<String> { emitter ->
            // Emit String 1
            emitter.onNext("1")
            // Emit complete event which terminates the Observable
            emitter.onComplete()
            // Emit String ? which will never be emitted to the subscribes because it is emitted after complete event.
            emitter.onNext("?")
        }.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") },
            onError = { println(it) }
        )
    }

    exampleOf("create error") {

        // Operator create creates an Observable that will specify all events on it.
        Observable.create<String> { emitter ->
            // Emit String 1
            emitter.onNext("1")
            // Emit error event which terminates the Observable
            emitter.onError(RuntimeException("Error"))
            // Emit complete event will never be emitted because the error occurred
            emitter.onComplete()
            // Emit String ? which will never be emitted to the subscribes because it is emitted after complete event.
            emitter.onNext("?")
        }.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") },
            onError = { println(it) }
        )
    }

    exampleOf("create memory leak") {

        // Operator create creates an Observable that will specify all events on it.
        Observable.create<String> { emitter ->
            // Emit String 1
            emitter.onNext("1")
            // Emit String ?
            emitter.onNext("?")
        }.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") },
            onError = { println(it) }
        )

        // Without complete and error event, the Observable will never finish and will never be disposed.
    }

    exampleOf("defer") {

        val subscriptions = CompositeDisposable()

        var flip = false

        // Instead of creating an Observable that waits for subscribers, you can create observable factories that will
        // provide new Observable to each subscriber
        val factory = Observable.defer {

            flip = !flip

            if (flip) {
                Observable.just(1, 2, 3)
            } else {
                Observable.just(4, 5, 6)
            }
        }

        // Each time you subscribe to the factory, you will get different Observable
        for (i in 0..3) {
            println("New subscription")
            subscriptions.add(
                factory.subscribe {
                    println(it)
                }
            )
        }

        subscriptions.dispose()
    }

    // Single is a type Observable that will emit success(value) or error event. Event success(value) is a combination
    // of next and complete events.
    exampleOf("single") {

        val subscriptions = CompositeDisposable()

        // Create a function that returns a Single
        fun loadText(fileName: String): Single<String> {
            return Single.create { emitter ->
                val file = File(fileName)

                // If file does not exists we emit error event
                if (!file.exists()) {
                    emitter.onError(FileNotFoundException("Can't find $fileName"))
                    return@create
                }
                val contents = file.readText(Charsets.UTF_8)
                // Emit contents of the file
                emitter.onSuccess(contents)
            }
        }

        val observer = loadText("Copyright.txt").subscribeBy(
            onSuccess = { println(it) },
            onError = { println("Error, $it") }
        )

        subscriptions.add(observer)
    }

    exampleOf("challenge observables") {
        val subscriptions = CompositeDisposable()

        val observable =
            Observable.never<Any>()
                // Operators that begin with doOn allow you to insert side effects that wont affect the Observable.
                .doOnSubscribe { println("Subscribed") }
                .doOnNext { println(it) }
                .doOnComplete { println("Completed") }
                .doOnDispose { println("Disposed") }

        val observer = observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )

        subscriptions.add(observer)
        subscriptions.dispose()

    }
}