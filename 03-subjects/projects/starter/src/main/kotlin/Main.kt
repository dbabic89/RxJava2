import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject

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

    // Subjects are Observables that provide a way to add new values onto an Observable and that will then be emitted to
    // subscribers. They are observables and observers at the same time.

    exampleOf("PublishSubject") {

        // PublishSubject starts empty and only emits new elements to subscribers. PublishSubject emits only to current
        // subscribers. Whatever was emitted before subscription, you wont get it when you subscribe.
        // Operator create creates a PublishSubject of type Integer, and it can only receive and publish Integers.
        val publishSubject = PublishSubject.create<Int>()

        // This sends new Integer into the subject but does not print anything out because there are no observers.
        publishSubject.onNext(0)

        // Adding new Observer to the publishSubject and creating a subscription. This Observer wont print 0, because it
        // was emitted before the subscription.
        val subscriptionOne = publishSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionOne", it) },
            onComplete = { printWithLabel("subscriptionOne", "Completed") }
        )

        // This sends new Integer into the subject and prints out because the publishSubscriber has a subscription.
        publishSubject.onNext(1)
        publishSubject.onNext(2)

        // Adding new Observer to the publishSubject and creating a subscription. This Observer wont print 0, 1 and 2
        // because it was emitted before the subscription.
        val subscriptionTwo = publishSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionTwo", it) },
            onComplete = { printWithLabel("subscriptionTwo", "Completed") }
        )

        // This sends new Integer into the subject and prints out because the publishSubscriber has a subscriptions. Both
        // subscriptionOne and subscriptionTwo will receive this Integer because it was emitted after they subscribed.
        publishSubject.onNext(3)

        // We are terminating subscriptionOne and it wont listen to new events from publishSubject.
        subscriptionOne.dispose()

        // This sends new Integer into the subject and prints out because the publishSubscriber still has a subscription.
        publishSubject.onNext(4)

        // We are terminating publishSubject with complete event and it will emit that event to its subscribers. This will
        // be emitted to all future subscribers.
        publishSubject.onComplete()

        // This sends new Integer into the subject but does not print anything out because the subject is terminated.
        publishSubject.onNext(5)

        subscriptionTwo.dispose()

        val subscriptionThree = publishSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionThree", it) },
            onComplete = { printWithLabel("subscriptionThree", "Completed") }
        )

        // This sends new Integer into the subject but does not print anything out because the subject is terminated.
        publishSubject.onNext(6)
    }

    exampleOf("BehaviourSubject") {

        // BehaviourSubject emits the latest next event to new subscribers.

        val subscriptions = CompositeDisposable()

        // BehaviourSubject created with initial value, but can also be created without it.
        val behaviorSubject = BehaviorSubject.createDefault("Initial value")

        // The subscription to the subject was created after the subject was and it replays the initial value.
        val subscriptionOne = behaviorSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionOne", it) },
            onError = { printWithLabel("subscriptionOne", it) }
        )

        subscriptions.add(subscriptionOne)

        // Adding new value to the subject that will become the latest value.
        behaviorSubject.onNext("X")

        // The subscription to the subject was created after the subject was and it replays the latest value.
        val subscriptionTwo = behaviorSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionTwo", it) },
            onError = { printWithLabel("subscriptionTwo", it) }
        )

        subscriptions.add(subscriptionTwo)

        // Adding RuntimeException error event to the subject.
        behaviorSubject.onError(RuntimeException("Error"))

        // The subscription to the subject was created after the subject was. After emitting error event, all subscribers
        // emit error event too.
        val subscriptionThree = behaviorSubject.subscribeBy(
            onNext = { printWithLabel("subscriptionThree", it) },
            onError = { printWithLabel("subscriptionThree", it) }
        )

        subscriptions.add(subscriptionThree)
    }

    exampleOf("BehaviourSubject state") {

        // BehaviourSubject allow you to access whatever the latest value is imperatively.

        val subscriptions = CompositeDisposable()

        val behaviorSubject = BehaviorSubject.createDefault(0)

        // Printing out the current value.
        println("behaviorSubject last emitted value: " + behaviorSubject.value)

        val subscriptionOne = behaviorSubject.subscribeBy { printWithLabel("subscriptionOne", it) }

        subscriptions.add(subscriptionOne)

        // Adding new value to the subject that will become the latest value.
        behaviorSubject.onNext(1)

        // Printing out the current value.
        println("behaviorSubject last emitted value: " + behaviorSubject.value)

        subscriptions.dispose()
    }

    exampleOf("ReplaySubject") {

        // ReplaySubject have a buffer of the latest elements they emit, and you specify the size. New subscriber gets
        // all the last x elements.

        val subscriptions = CompositeDisposable()

        // Creating ReplaySubject with the buffer size of 2.
        val replaySubject = ReplaySubject.createWithSize<String>(2)

        // Adding three elements to the subject. When we subscribe to this ReplaySubject, it will emit only 2 and 3.
        replaySubject.onNext("1")
        replaySubject.onNext("2")
        replaySubject.onNext("3")

        val subscriptionOne = replaySubject.subscribeBy(
            onNext = { printWithLabel("subscriptionOne", it) },
            onError = { printWithLabel("subscriptionOne", it) }
        )

        subscriptions.add(subscriptionOne)

        val subscriptionTwo = replaySubject.subscribeBy(
            onNext = { printWithLabel("subscriptionTwo", it) },
            onError = { printWithLabel("subscriptionTwo", it) }
        )

        subscriptions.add(subscriptionTwo)

        // Adding new element to the subject will trigger next event in all subscribers.
        replaySubject.onNext("4")

        // Adding new subscriber to the subject will get last two items from the subject which are 3 and 4.
        val subscriptionThree = replaySubject.subscribeBy(
            onNext = { printWithLabel("subscriptionThree", it) },
            onError = { printWithLabel("subscriptionThree", it) }
        )

        subscriptions.add(subscriptionThree)

        // Adding error event to the subject will terminate it and trigger error event on all previous subscribers.
        replaySubject.onError(RuntimeException("Error"))

        // Adding new subscriber to the subject will get last two items from the subject which are 3 and 4 and
        // it will throw error event.
        val subscriptionFour = replaySubject.subscribeBy(
            onNext = { printWithLabel("subscriptionFour", it) },
            onError = { printWithLabel("subscriptionFour", it) }
        )

        subscriptions.add(subscriptionFour)

        subscriptions.dispose()
    }

    exampleOf("AsyncSubject complete") {

        // AsyncSubject emit only the last value they receive before the complete event. If the subject receives an error
        // event the subscribers wont see anything. It is useful if you want to display the final value of some event.

        val subscriptions = CompositeDisposable()

        val asyncSubjectComplete = AsyncSubject.create<Int>()

        val subscriptionOne = asyncSubjectComplete.subscribeBy(
            onNext = { printWithLabel("subscriptionOne", it) },
            onComplete = { printWithLabel("subscriptionOne", "Completed") }
        )

        subscriptions.add(subscriptionOne)

        asyncSubjectComplete.onNext(0)
        asyncSubjectComplete.onNext(1)
        asyncSubjectComplete.onNext(2)

        // Emitting complete event will send latest value to the subscribers which is 2.
        asyncSubjectComplete.onComplete()

        subscriptions.dispose()
    }

    exampleOf("AsyncSubject error") {

        val subscriptions = CompositeDisposable()

        val asyncSubjectComplete = AsyncSubject.create<Int>()

        asyncSubjectComplete.onNext(0)
        asyncSubjectComplete.onNext(1)
        asyncSubjectComplete.onNext(2)

        val asyncSubjectError = AsyncSubject.create<Int>()

        val subscriptionOne = asyncSubjectError.subscribeBy(
            onNext = { printWithLabel("subscriptionOne", it) },
            onError = { printWithLabel("subscriptionOne", it) },
            onComplete = { printWithLabel("subscriptionOne", "Completed") }
        )

        subscriptions.add(subscriptionOne)

        asyncSubjectError.onNext(0)
        asyncSubjectError.onNext(1)
        asyncSubjectError.onNext(2)

        // Emitting error event will not send any value to the subscribers and will trigger error event.
        asyncSubjectError.onError(RuntimeException("Error"))

        subscriptions.dispose()
    }

    exampleOf("challenge subjects") {

        val subscriptions = CompositeDisposable()

        val dealtHand = PublishSubject.create<List<Pair<String, Int>>>()

        fun deal(cardCount: Int) {
            val deck = cards
            var cardsRemaining = 52
            val hand = mutableListOf<Pair<String, Int>>()

            (0 until cardCount).forEach { _ ->
                val randomIndex = (0 until cardsRemaining).random()
                hand.add(deck[randomIndex])
                deck.removeAt(randomIndex)
                cardsRemaining -= 1
            }

            // Add code to update dealtHand here

            val points = points(hand)

            if (points > 21) {
                dealtHand.onError(HandError.Busted())
            } else {
                dealtHand.onNext(hand)
            }
        }

        // Add subscription to dealtHand here

        val player = dealtHand.subscribeBy(
            onNext = { println("Cards in hand: " + cardString(it) + " Total points: " + points(it)) },
            onError = { println(it) }
        )

        subscriptions.add(player)

        deal(3)
    }
}