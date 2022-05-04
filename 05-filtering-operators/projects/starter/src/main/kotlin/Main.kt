import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject

fun main(args: Array<String>) {


    exampleOf("ignoreElements") {

        // The ignoreElements operator ignores the next event elements and only listens for complete or error events.
        // It is useful when you want to know when an Observable is terminated.

        val subscriptions = CompositeDisposable()

        val strikes = PublishSubject.create<String>()

        subscriptions.add(
            strikes
                // ignoreElements returns a Completable so there is no onNext event in subscribeBy
                .ignoreElements()
                .subscribeBy { println("You are out!") }
        )

        // Even though we are emitting new elements in next event, nothing is printed.
        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onNext("X")

        // Triggering complete event will notify the subscriber
        strikes.onComplete()

        subscriptions.dispose()
    }

    exampleOf("elementAt") {

        val subscriptions = CompositeDisposable()

        val strikes = PublishSubject.create<String>()

        subscriptions.add(
            strikes
                // elementAt returns a Maybe because the Observable might not have the third element, and we subscribe
                // with onSuccess instead of onNext.
                .elementAt(2)
                .subscribeBy(
                    onSuccess = { println("You are out!") }
                )
        )

        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onNext("X")

        subscriptions.dispose()
    }

    exampleOf("filter") {

        // Operator filter takes a predicate lambda and applies it to each element, letting through only those elements
        // for which the predicate is true.

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .filter { number -> number > 5 }
                .subscribeBy(
                    onNext = { println(it) },
                    onComplete = { println("Completed") }
                )
        )

        subscriptions.dispose()
    }

    exampleOf("skip") {

        // Operator skip allows you to ignore first x elements.

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just("A", "B", "C", "D", "E", "F")
                .skip(3)
                .subscribeBy(
                    onNext = { println(it) },
                    onComplete = { println("Completed") }
                )
        )

        subscriptions.dispose()
    }

    exampleOf("skipWhile") {

        // Operator skipWhile allows you to skip elements until something is not skipped, and after that it will let
        // everything else through. Opposite from operator filter, operator skipWhile will skip item if the condition
        // is true, and let it through if the condition is false.

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just(2, 2, 3, 4)
                .skipWhile { number -> number % 2 == 0 }
                .subscribeBy(
                    onNext = { println(it) },
                    onComplete = { println("Completed") }
                )
        )

        subscriptions.dispose()
    }

    exampleOf("skipUntil") {

        // Operator skipUntil will skip elements until some other trigger observable emits.

        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subscriptions.add(
            subject.skipUntil(trigger)
                .subscribeBy { println(it) }
        )

        // Nothing is printed out, because we are skipping
        subject.onNext("A")
        subject.onNext("B")

        // Trigger will stop skipUntil operator and all next elements will be printed.
        trigger.onNext("X")

        subject.onNext("C")

        subscriptions.dispose()
    }

    exampleOf("take") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just(1, 2, 3, 4, 5, 6)
                .take(3)
                .subscribeBy(
                    onNext = { println(it) },
                    onComplete = { println("Completed") }
                )
        )

        subscriptions.dispose()
    }

    exampleOf("takeWhile") {

        // Operator takeWhile lets through elements that meet the condition and come in before the condition is met

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1))
                .takeWhile { number -> number < 5 }
                .subscribeBy(
                    onNext = { println(it) },
                    onComplete = { println("Completed") }
                )
        )

        subscriptions.dispose()
    }

    exampleOf("takeUntil") {

        // Operator takeUntil will take elements until the trigger observable emits an element

        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subscriptions.add(
            subject.takeUntil(trigger)
                .subscribeBy { println(it) }
        )

        // This is printed out, because we are taking elements until trigger emits an element
        subject.onNext("1")
        subject.onNext("2")

        // Trigger will stop takeUntil operator and all next elements will be ignored.
        trigger.onNext("X")

        subject.onNext("3")

        subscriptions.dispose()
    }

    exampleOf("distinctUntilChanged") {

        // Operator distinctUntilChanged prevents duplicated elements that are next to each other

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just("Dog", "Cat", "Cat", "Dog")
                .distinctUntilChanged()
                .subscribe { println(it) }
        )

        subscriptions.dispose()
    }

    exampleOf("distinctUntilChangedPredicate") {

        // Operator distinctUntilChanged with Predicate prevents duplicated elements that are next to each other based on
        // provided condition

        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just("ABC", "BCD", "CDE", "FGH", "IJK", "JKL", "LMN")
                // This predicate will convert both strings to list of strings and check if elements of second list appear
                // in the first list. If they appear, it is a duplicate and we wont print them.
                .distinctUntilChanged { first, second ->
                    second.toList().any { it in first.toList() }
                }
                .subscribe { println(it) }
        )

        subscriptions.dispose()
    }

    exampleOf("Challenge 1") {

        val subscriptions = CompositeDisposable()

        val contacts = mapOf(
            "603-555-1212" to "Florent",
            "212-555-1212" to "Junior",
            "408-555-1212" to "Marin",
            "617-555-1212" to "Scott"
        )

        fun phoneNumberFrom(inputs: List<Int>): String {
            val phone = inputs.map { it.toString() }.toMutableList()
            phone.add(3, "-")
            phone.add(7, "-")
            return phone.joinToString("")
        }

        val input = PublishSubject.create<Int>()

        // Add your code here

        subscriptions.add(
            input
                .skipWhile { it == 0 }
                .filter { it < 10 }
                .take(10)
                .toList()
                .subscribeBy(
                    onSuccess = {
                        val phoneNumber = phoneNumberFrom(it)
                        val contact = contacts[phoneNumber]

                        if (contact != null) {
                            println("Dialing $contact ($phoneNumber)")
                        } else {
                            println("Contact not found")
                        }
                    }
                )
        )

        input.onNext(0)
        input.onNext(603)

        input.onNext(2)
        input.onNext(1)

        // Confirm that 7 results in "Contact not found", and then change to 2 and confirm that Junior is found
        input.onNext(2)

        "5551212".forEach {
            input.onNext(it.toString().toInt()) // Need toString() or else Char conversion is done
        }

        input.onNext(9)
    }
}
