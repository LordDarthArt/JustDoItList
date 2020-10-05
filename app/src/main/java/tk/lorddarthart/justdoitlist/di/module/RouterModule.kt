package tk.lorddarthart.justdoitlist.di.module

import dagger.Module
import dagger.Provides
import tk.lorddarthart.justdoitlist.R
import tk.lorddarthart.justdoitlist.router.Router
import tk.lorddarthart.justdoitlist.router.RouterImpl
import tk.lorddarthart.justdoitlist.util.converters.PriorityConverter
import tk.lorddarthart.justdoitlist.util.converters.PriorityConverterImpl
import tk.lorddarthart.smartnavigation.SmartNavigator
import javax.inject.Singleton

@Module
class RouterModule {
    @Provides
    @Singleton
    fun provideRouter(): Router {
        return RouterImpl(
            baseNavigator = SmartNavigator(R.id.fragment_base_container),
            mainNavigator = SmartNavigator(R.id.fragment_main_container),
            authNavigator = SmartNavigator(R.id.fragment_enter)
        )
    }
}