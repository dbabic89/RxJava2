import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.withLatestFrom
import io.reactivex.rxjava3.subjects.PublishSubject

fun main(args: Array<String>) {

    exampleOf("startWith") {

        // Operator startWith creates an observable with the same type, that will emit initial value.

        val subscriptions = CompositeDisposable()

        val missingNumbers = Observable.just(3, 4, 5)

        val completeSet = missingNumbers.startWithIterable(listOf(1, 2))
        completeSet
            .subscribe { number ->
                println(number)
            }
            .addTo(subscriptions)
    }

    exampleOf("concat") {

        // Operator concat chains two sequences. If any of the inner observables emits an error the concatenated
        // observable will emit error and terminate.

        val subscriptions = CompositeDisposable()

        val first = Observable.just(1, 2, 3)
        val second = Observable.just(4, 5, 6)

        Observable.concat(first, second)
            .subscribe { number ->
                println(number)
            }
            .addTo(subscriptions)
    }

    exampleOf("concatWith") {

        val subscriptions = CompositeDisposable()

        val germanCities = Observable.just("Berlin", "Münich", "Frankfurt")
        val spanishCities = Observable.just("Madrid", "Barcelona", "Valencia")

        germanCities
            .concatWith(spanishCities)
            .subscribe { number ->
                println(number)
            }
            .addTo(subscriptions)
    }

    exampleOf("concatMap") {

        // Operator concatMap guaranties the order.

        val subscriptions = CompositeDisposable()

        val countries = Observable.just("Germany", "Spain")

        val observable = countries
            .concatMap {
                when (it) {
                    "Germany" -> Observable.just("Berlin", "Münich", "Frankfurt")
                    "Spain" -> Observable.just("Madrid", "Barcelona", "Valencia")
                    else -> Observable.empty<String>()
                }
            }

        observable
            .subscribe { city ->
                println(city)
            }
            .addTo(subscriptions)
    }

    exampleOf("merge") {

        // Operator merge subscribes to each sequence and emits elements as they arrive. It completes when all internal
        // sequences complete and if any emits an error, observable will emit error too and terminate.

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<Int>()
        val right = PublishSubject.create<Int>()

        Observable.merge(left, right)
            .subscribe {
                println(it)
            }
            .addTo(subscriptions)

        left.onNext(0)
        left.onNext(1)
        right.onNext(3)
        left.onNext(4)
        right.onNext(5)
        right.onNext(6)
    }

    exampleOf("mergeWith") {

        val subscriptions = CompositeDisposable()

        val germanCities = PublishSubject.create<String>()
        val spanishCities = PublishSubject.create<String>()

        germanCities.mergeWith(spanishCities)
            .subscribe {
                println(it)
            }
            .addTo(subscriptions)

        germanCities.onNext("Frankfurt")
        germanCities.onNext("Berlin")
        spanishCities.onNext("Madrid")
        germanCities.onNext("Münich")
        spanishCities.onNext("Barcelona")
        spanishCities.onNext("Valencia")
    }

    exampleOf("combineLatest") {

        // Operator combineLatest takes latest elements from each sequence. They can be different type. Nothing happens
        // before each observable emits one element and after that every time one observable emits an element, the
        // observable will combine and emit all the latest elements

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()

        Observables.combineLatest(left, right) { leftString, rightString ->
            "$leftString $rightString"
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        left.onNext("Hello")
        right.onNext("World")
        left.onNext("It's nice to")
        right.onNext("be here!")
        left.onNext("Actually, it's super great to")
    }

    exampleOf("zip") {

        // Operator zip waits for each inner observable to emit new value and if one completes the zip operator completes
        // too.

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()

        Observables.zip(left, right) { weather, city ->
            "It's $weather in $city"
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        left.onNext("sunny")
        right.onNext("Lisbon")
        left.onNext("cloudy")
        right.onNext("Copenhagen")
        left.onNext("cloudy")
        right.onNext("London")
        left.onNext("sunny")
        right.onNext("Madrid")
        right.onNext("Vienna")
    }

    exampleOf("withLatestFrom") {

        // Operator withLatestFrom combines latest values from observables but only when particular trigger occurs.

        val subscriptions = CompositeDisposable()

        val button = PublishSubject.create<Unit>()
        val editText = PublishSubject.create<String>()

        button.withLatestFrom(editText) { _: Unit, value: String ->
            value
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        editText.onNext("Par")
        editText.onNext("Pari")
        editText.onNext("Paris")
        button.onNext(Unit)
        button.onNext(Unit)
    }

    exampleOf("sample") {

        // Operator sample emits latest values from the observable after a trigger. If there is no new data after last
        // trigger it wont emit anything.

        val subscriptions = CompositeDisposable()

        val button = PublishSubject.create<Unit>()
        val editText = PublishSubject.create<String>()

        editText.sample(button)
            .subscribe {
                println(it)
            }.addTo(subscriptions)

        editText.onNext("Par")
        editText.onNext("Pari")
        editText.onNext("Paris")
        button.onNext(Unit)
        button.onNext(Unit)
    }

    exampleOf("amb") {

        // operator amb waits for an observable to emit and then unsubscribes from the other one

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()

        left.ambWith(right).subscribe {
            println(it)
        }
            .addTo(subscriptions)

        left.onNext("Lisbon")
        right.onNext("Copenhagen")
        left.onNext("London")
        left.onNext("Madrid")
        right.onNext("Vienna")
    }

    exampleOf("reduce") {

        // Operator reduce starts with initial value and accumulates a summary value. When the observable completes,
        // reduce emits the summary value.

        val subscriptions = CompositeDisposable()

        val source = Observable.just(1, 3, 5, 7, 9)
        source
            .reduce(0) { a, b -> a + b }
            .subscribeBy(onSuccess = {
                println(it)
            })
            .addTo(subscriptions)
    }

    exampleOf("scan") {

        // Operator scan starts with initial value and every time the observable emits a value, scan executes lambda and
        // emits the value.

        val subscriptions = CompositeDisposable()

        val source = Observable.just(1, 3, 5, 7, 9)

        source
            .scan(0) { a, b -> a + b }
            .subscribe {
                println(it)
            }
            .addTo(subscriptions)
    }

    exampleOf("challenge scan") {

        val subscriptions = CompositeDisposable()

        val source = Observable.just(1, 3, 5, 7, 9)

        val scan = source.scan(0) { a, b -> a + b }

        val observable = Observables.zip(source, scan) { value: Int, total: Int -> value to total }

        observable
            .subscribe {
                println("Value: ${it.first} Running total = ${it.second}")
            }
            .addTo(subscriptions)
    }
}