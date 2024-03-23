package com.example.justin_yan_myruns5

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EntryRepository (private val entryDatabaseDao: EntryDatabaseDao) {

    val allEntries: Flow<List<Entry>> = entryDatabaseDao.getAllEntries()

    fun insert(entry: Entry){
        CoroutineScope(IO).launch{
            entryDatabaseDao.insertEntry(entry)
        }
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            entryDatabaseDao.deleteEntry(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            entryDatabaseDao.deleteAll()
        }
    }
}