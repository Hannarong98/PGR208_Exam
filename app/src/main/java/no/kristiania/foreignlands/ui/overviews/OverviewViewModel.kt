package no.kristiania.foreignlands.ui.overviews

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.kristiania.foreignlands.data.repository.OverviewRepository
import no.kristiania.foreignlands.data.model.overviews.Places


class OverviewViewModel(private val repository: OverviewRepository) : ViewModel() {

    val placesLiveData = MutableLiveData<MutableList<Places>>()

    fun fetchPlaces(){
        viewModelScope.launch {
            val places = repository.getPlaces()
            placesLiveData.postValue(places)
        }
    }

}
