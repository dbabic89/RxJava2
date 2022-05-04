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
package com.raywenderlich.android.quicktodo.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.android.quicktodo.model.TaskItem
import com.raywenderlich.android.quicktodo.repository.RoomTaskRepository
import com.raywenderlich.android.quicktodo.repository.TaskRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit

class TodoListViewModel(
    repository: TaskRepository,
    backgroundScheduler: Scheduler,
    computationScheduler: Scheduler,
    taskClicks: Observable<TaskItem>,
    taskDoneToggles: Observable<Pair<TaskItem, Boolean>>,
    addClicks: Observable<Unit>,
    swipeStream: Observable<TaskItem>
) : ViewModel() {

    val listItemsLiveData = MutableLiveData<List<TodoListItem>>()
    val showEditTaskLiveData = MutableLiveData<Int>()
    val statisticsData = MutableLiveData<Pair<Int, Int>>()

    private val disposables = CompositeDisposable()

    init {
        val tasksStream = repository
            .taskStream()
            .cache()

        tasksStream
            .map { tasks -> tasks.map { TodoListItem.TaskListItem(it) } }
            .map { listItems ->
                val finishedTasks = listItems.filter { it.task.isDone }
                val todoTasks = listItems - finishedTasks
                listOf(
                    TodoListItem.DueTasks,
                    *todoTasks.toTypedArray(),
                    TodoListItem.DoneTasks,
                    *finishedTasks.toTypedArray()
                )
            }
            .subscribeOn(backgroundScheduler)
            .subscribe(object : Observer<List<TodoListItem>> {
                override fun onSubscribe(d: Disposable) {
                    disposables.add(d)
                }

                override fun onNext(t: List<TodoListItem>) {
                    listItemsLiveData.postValue(t)
                    Log.d("abcd", "onNext")
                }

                override fun onError(e: Throwable) {
                    Log.d("abcd", "onError")
                }

                override fun onComplete() {
                    Log.d("abcd", "onComplete")
                }

            })
//            .subscribe(listItemsLiveData::postValue)
//            .addTo(disposables)

        tasksStream
            .map { tasks -> tasks.map { TodoListItem.TaskListItem(it) } }
            .map { listItems ->
                val finishedTasks = listItems.filter { it.task.isDone }
                val todoTasks = listItems - finishedTasks
                Pair(todoTasks.size, finishedTasks.size)
            }
            .subscribeOn(backgroundScheduler)
            .subscribe(statisticsData::postValue)
            .addTo(disposables)

        taskDoneToggles
            .flatMapSingle { newItemPair ->
                repository
                    .insertTask(newItemPair.first.copy(isDone = newItemPair.second))
                    .subscribeOn(backgroundScheduler)
            }
            .subscribe()
            .addTo(disposables)

        taskClicks
            .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
            .subscribe {
                val id = it.id ?: RoomTaskRepository.INVALID_ID
                showEditTaskLiveData.postValue(id)
            }
            .addTo(disposables)

        addClicks
            .throttleFirst(1, TimeUnit.SECONDS, computationScheduler)
            .subscribe { showEditTaskLiveData.postValue(RoomTaskRepository.INVALID_ID) }
            .addTo(disposables)

        swipeStream
            .flatMapCompletable {
                repository.deleteTask(it).subscribeOn(backgroundScheduler)
            }
            .subscribe()
            .addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
