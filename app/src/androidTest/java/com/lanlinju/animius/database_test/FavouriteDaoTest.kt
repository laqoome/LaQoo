package com.laqoome.laqoo.database_test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.laqoome.laqoo.data.local.dao.FavouriteDao
import com.laqoome.laqoo.data.local.database.laqooDatabase
import com.laqoome.laqoo.data.local.entity.FavouriteEntity
import com.laqoome.laqoo.util.SourceMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest {
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var laqooDatabase: laqooDatabase

    private var favourite1 = FavouriteEntity(1, "海贼王1", "/video1", "img1", SourceMode.Yhdm.name)
    private var favourite2 = FavouriteEntity(2, "海贼王2", "/video2", "img2", SourceMode.Yhdm.name)

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        laqooDatabase = Room.inMemoryDatabaseBuilder(context, laqooDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favouriteDao = laqooDatabase.favouriteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        laqooDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsFavouriteIntoDB() = runBlocking {
        addOneFavouriteToDb()
        val allFavourites = favouriteDao.getAllFavourites().first()
        assertEquals(allFavourites[0], favourite1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllFavourites_returnsAllFavouritesFromDB() = runBlocking {
        addTwoFavouriteToDb()
        val allFavourites = favouriteDao.getAllFavourites().first()
        assertEquals(allFavourites[0], favourite1)
        assertEquals(allFavourites[1], favourite2)
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteFavourites_deletesAllFavouritesFromDB() = runBlocking {
        addTwoFavouriteToDb()
        favouriteDao.deleteFavourite(favourite1)
        favouriteDao.deleteFavourite(favourite2)
        val allItems = favouriteDao.getAllFavourites().first()
        assertTrue(allItems.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun daoGetFavourite_returnsFavouriteFromDB() = runBlocking {
        addOneFavouriteToDb()
        val favourite = favouriteDao.getFavouriteByDetailUrl("/video1")
        assertEquals(favourite.first(), favourite1)
    }

    @Test
    @kotlin.jvm.Throws(Exception::class)
    fun daoCheckFavourite_returnsNullFromDB() = runBlocking {
        val favourite = favouriteDao.checkFavourite("/video3").first()
        assertNull(favourite)
    }

    @Test
    @kotlin.jvm.Throws(Exception::class)
    fun daoCheckFavourite_returnsNotNullFromDB() = runBlocking {
        addOneFavouriteToDb()
        val favourite = favouriteDao.checkFavourite(favourite1.detailUrl).first()
        assertNotNull(favourite)
    }

    private suspend fun addOneFavouriteToDb() {
        favouriteDao.insertFavourite(favourite1)
    }

    private suspend fun addTwoFavouriteToDb() {
        favouriteDao.insertFavourite(favourite1)
        favouriteDao.insertFavourite(favourite2)
    }
}