package app

import api.Api
import api.ApiImpl
import area.AreaQueries
import area.AreaResultModel
import area.AreasModel
import area.AreaModel
import area.AreasRepo
import conf.ConfQueries
import conf.ConfRepo
import element.ElementQueries
import element.ElementsRepo
import event.EventQueries
import event.EventsModel
import event.EventsRepo
import kotlinx.serialization.json.Json
import location.UserLocationRepository
import map.MapModel
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import reports.ReportQueries
import reports.ReportsModel
import reports.ReportsRepo
import search.SearchModel
import search.SearchResultModel
import sync.Sync
import user.UserQueries
import user.UsersModel
import user.UsersRepo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.bind

val appModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(BrotliInterceptor)
//            .addInterceptor {
//                Log.d("okhttp", it.request().url.toString())
//                it.proceed(it.request())
//            }
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    single {
        Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }

    single {
        ApiImpl(
            baseUrl = "https://api.btcmap.org/v2".toHttpUrl(),
            httpClient = get(),
            json = get(),
        )
    }.bind(Api::class)

    singleOf(::AreaQueries)
    singleOf(::AreasRepo)
    viewModelOf(::AreasModel)
    viewModelOf(::AreaModel)
    viewModelOf(::AreaResultModel)

    singleOf(::ElementQueries)
    singleOf(::ElementsRepo)

    singleOf(::ConfQueries)
    singleOf(::ConfRepo)

    singleOf(::EventQueries)
    singleOf(::EventsRepo)
    viewModelOf(::EventsModel)

    singleOf(::UserLocationRepository)

    singleOf(::ReportQueries)
    singleOf(::ReportsRepo)
    viewModelOf(::ReportsModel)

    singleOf(::Sync)

    singleOf(::UserQueries)
    singleOf(::UsersRepo)
    viewModelOf(::UsersModel)

    viewModelOf(::MapModel)

    viewModelOf(::SearchModel)
    viewModelOf(::SearchResultModel)
}