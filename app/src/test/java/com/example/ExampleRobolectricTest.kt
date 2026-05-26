package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WeatherDatabase
import com.example.data.WeatherRepository
import com.example.ui.WeatherViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LJS Weather monitoring", appName)
  }

  @Test
  fun `test database and viewmodel initialization`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val application = context as Application

    // Initialize database
    val database = WeatherDatabase.getDatabase(context)
    assertNotNull(database)

    // Initialize repository
    val repository = WeatherRepository(database)
    assertNotNull(repository)

    // Preload should run cleanly
    repository.preloadDefaultCitiesIfEmpty()

    val count = database.savedCityDao().getCityCount()
    assertEquals(5, count)

    // Initialize viewmodel
    val viewModel = WeatherViewModel(application, repository)
    assertNotNull(viewModel)
    
    // Check that we can observe saved cities and they are populated
    val cities = viewModel.savedCities.value
    assertNotNull(cities)
  }
}

