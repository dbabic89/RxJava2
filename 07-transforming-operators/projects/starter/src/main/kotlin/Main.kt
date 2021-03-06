import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

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

    exampleOf("toList") {

        val subscriptions = CompositeDisposable()

        val items = Observable.just("A", "B", "C")

        subscriptions.add(
            items
                // Transforms elements in a list and the list will be printed out.
                .toList()
                .subscribeBy { println(it) }
        )
    }

    exampleOf("map") {

        // Operator map transforms the observable

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable
                .just("M", "C", "V", "I")
                .map { it.romanNumeralIntValue() }
                .subscribeBy { println(it) }
        )

    }

    exampleOf("flatMap") {

        // Operator flatMap transforms all values from all observables and flattens them all down to a target observable.

        val subscriptions = CompositeDisposable()

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student
            .flatMap { it.score }
            .subscribe { println(it) }
            .addTo(subscriptions)

        student.onNext(ryan)

        ryan.score.onNext(85)

        student.onNext(charlotte)

        ryan.score.onNext(95)

        charlotte.score.onNext(100)
    }

    exampleOf("switchMap") {

        // Operator switchMap takes a function and applies it to each item and returns an observable that emits only items
        // from the reactive source that was emitted last.

        val subscriptions = CompositeDisposable()

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student
            .switchMap { it.score }
            .subscribe { println(it) }
            .addTo(subscriptions)

        student.onNext(ryan)

        ryan.score.onNext(85)

        student.onNext(charlotte)

        // This will not be printed because the charlotte subject already emitted and switchMap now only emits its values.
        ryan.score.onNext(95)

        charlotte.score.onNext(100)
    }

    exampleOf("materialize / dematerialize") {

        val subscriptions = CompositeDisposable()

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        val studentScore = student.switchMap { it.score.materialize() }

        studentScore
            .filter {
                if (it.error != null) {
                    println(it.error)
                    false
                } else {
                    true
                }
            }
            .dematerialize { it }
            .subscribe { println(it) }
            .addTo(subscriptions)

        student.onNext(ryan)

        ryan.score.onNext(85)

        ryan.score.onError(RuntimeException("Error"))

        ryan.score.onNext(90)

        student.onNext(charlotte)
    }
}
