package com.bubelov.coins.dagger

import android.content.Context
import com.bubelov.coins.App

import com.bubelov.coins.repository.area.NotificationAreaRepository
import com.bubelov.coins.repository.currency.CurrenciesRepository
import com.bubelov.coins.repository.place.PlacesRepository
import com.bubelov.coins.repository.placecategory.PlaceCategoriesDataSourceApi
import com.bubelov.coins.repository.placecategory.PlaceCategoriesDataSourceDb
import com.bubelov.coins.repository.placecategory.PlaceCategoriesDataSourceMemory
import com.bubelov.coins.repository.placecategory.PlaceCategoriesRepository
import com.bubelov.coins.repository.rate.ExchangeRatesRepository
import com.bubelov.coins.ui.fragment.SettingsFragment
import com.bubelov.coins.ui.fragment.SignInFragment
import com.bubelov.coins.ui.fragment.SignUpFragment
import com.bubelov.coins.ui.viewmodel.ExchangeRatesViewModel
import com.bubelov.coins.ui.viewmodel.MainViewModel
import com.bubelov.coins.util.PlaceNotificationManager

import javax.inject.Singleton

import dagger.Component
import dagger.BindsInstance
import dagger.android.AndroidInjectionModule

/**
 * @author Igor Bubelov
 */

@Singleton
@Component(modules = arrayOf(AppModule::class, AndroidInjectionModule::class, ActivityBuilder::class, ServiceBuilder::class))
interface AppComponent {
    fun notificationManager(): PlaceNotificationManager

    fun notificationAreaRepository(): NotificationAreaRepository
    fun placesRepository(): PlacesRepository
    fun currenciesRepository(): CurrenciesRepository
    fun exchangeRatesRepository(): ExchangeRatesRepository
    fun placeCategoriesRepository(): PlaceCategoriesRepository
    fun placeCategoriesDataSourceNetwork(): PlaceCategoriesDataSourceApi
    fun placeCategoriesDataSourceDb(): PlaceCategoriesDataSourceDb
    fun placeCategoriesDataSourceMemory(): PlaceCategoriesDataSourceMemory

    fun inject(app: App)

    fun inject(target: SignInFragment)
    fun inject(target: SignUpFragment)
    fun inject(target: SettingsFragment)

    fun inject(target: MainViewModel)
    fun inject(target: ExchangeRatesViewModel)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}