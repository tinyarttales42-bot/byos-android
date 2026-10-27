package app.byos.android

import android.app.Application
import androidx.room.Room
import app.byos.android.data.LibraryDatabase
import app.byos.android.data.LibraryRepository

class ByosApplication : Application() {
    private val database by lazy { Room.databaseBuilder(this, LibraryDatabase::class.java, "byos.db").build() }
    val repository by lazy { LibraryRepository(contentResolver, database.libraryDao()) }
}
