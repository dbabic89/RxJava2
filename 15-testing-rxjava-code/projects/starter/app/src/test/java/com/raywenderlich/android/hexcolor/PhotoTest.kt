package com.raywenderlich.android.hexcolor

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test

class Photo

interface PhotoProvider {
    fun photoObservable(): Observable<Photo>
}

class PhotoViewModel(provider: PhotoProvider) {

    var disableButton = false
    private var photoList = arrayListOf<Photo>()

    init {
        provider.photoObservable().subscribe {
            photoList.add(it)
            if (photoList.size >= 5) {
                disableButton = true
            }
        }
    }
}

class PhotosTest {
    @Test
    fun `button disabled after 5 photos`() {
        val subject = PublishSubject.create<Photo>()
        val photoProviderMock = object : PhotoProvider {
            override fun photoObservable(): Observable<Photo> {
                return subject
            }
        }

        val viewModel = PhotoViewModel(photoProviderMock)

        subject.onNext(Photo())
        Assert.assertFalse(viewModel.disableButton)
        subject.onNext(Photo())
        subject.onNext(Photo())
        subject.onNext(Photo())
        Assert.assertFalse(viewModel.disableButton)
        subject.onNext(Photo())
        Assert.assertTrue(viewModel.disableButton)
    }
}
