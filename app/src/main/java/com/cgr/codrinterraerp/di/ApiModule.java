package com.cgr.codrinterraerp.di;

import com.cgr.codrinterraerp.BuildConfig;
import com.cgr.codrinterraerp.db.dao.ApiLogsDao;
import com.cgr.codrinterraerp.services.IAuthApiService;
import com.cgr.codrinterraerp.services.IMasterApiService;
import com.cgr.codrinterraerp.services.interceptors.ApiLoggingInterceptor;
import com.cgr.codrinterraerp.services.interceptors.DynamicAuthInterceptor;
import com.cgr.codrinterraerp.utils.AppLogger;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(ApiLogsDao apiLogsDao) {

        AppLogger.d(getClass(), "OkHttpClient Created Once");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        ApiLoggingInterceptor apiLoggingInterceptor = new ApiLoggingInterceptor(apiLogsDao);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS);

        // ✅ Add auth first
        httpClient.addInterceptor(new DynamicAuthInterceptor());

        // ✅ API Logs
        httpClient.addInterceptor(apiLoggingInterceptor);

        // ✅ Add DB logging (only debug OR customize inside interceptor)
        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(logging);
        }

        return httpClient.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public IAuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(IAuthApiService.class);
    }

    @Provides
    @Singleton
    public IMasterApiService provideMasterApiService(Retrofit retrofit) {
        return retrofit.create(IMasterApiService.class);
    }
}