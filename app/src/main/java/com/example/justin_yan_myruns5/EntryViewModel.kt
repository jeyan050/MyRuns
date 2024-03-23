package com.example.justin_yan_myruns5

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class EntryViewModel(private val repository: EntryRepository) : ViewModel() {
    val allEntriesLiveData: LiveData<List<Entry>> = repository.allEntries.asLiveData()

    fun insert(entry: Entry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(entry)
        }
    }

    fun deleteFirst(){
        val entryList = allEntriesLiveData.value
        if (!entryList.isNullOrEmpty()){
            val id = entryList[0].id
            repository.delete(id)
        }
    }

    fun delete(position: Int){
        val entryList = allEntriesLiveData.value
        if (!entryList.isNullOrEmpty()){
            val id = entryList[position].id
            repository.delete(id)
        }
    }

    fun deleteAll(){
        val entryList = allEntriesLiveData.value
        if (!entryList.isNullOrEmpty())
            repository.deleteAll()
    }
}

class EntryViewModelFactory (private val repository: EntryRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is EntryViewModel in this case.
        if(modelClass.isAssignableFrom(EntryViewModel::class.java))
            return EntryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}