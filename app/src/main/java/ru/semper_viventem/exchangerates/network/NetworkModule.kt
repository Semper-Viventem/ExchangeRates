package ru.semper_viventem.exchangerates.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.semper_viventem.exchangerates.BuildConfig
import java.util.concurrent.TimeUnit

object NetworkModule {
    val module = module {

        single {
            Retrofit.Builder()
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BuildConfig.BASE_URL)
                .build()
                .create(Api::class.java)
        }

        single {
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .build()
        }
    }
}