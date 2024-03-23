package com.example.justin_yan_myruns5
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class FragmentHistory : Fragment() {

    private lateinit var arrayAdapter: MyListAdapter

    // Create history fragment view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Get List on layout
        var listView = view.findViewById<ListView>(R.id.list)

        arrayAdapter = MyListAdapter(requireActivity(), ArrayList() )
        listView.adapter = arrayAdapter

        // Database
        var database = EntryDatabase.getInstance(requireActivity())
        var databaseDao = database.entryDatabaseDao
        var repository = EntryRepository(databaseDao)
        var viewModelFactory = EntryViewModelFactory(repository)
        var entryViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(EntryViewModel::class.java)

        // Get Entries
        Log.d("Entries:",  entryViewModel.allEntriesLiveData.toString())
        entryViewModel.allEntriesLiveData.observe(requireActivity(), Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })

        // Inflate the layout for this fragment
        return view
    }

    override fun onResume() {
        super.onResume()
        // Force the adapter to refresh the list
        arrayAdapter.notifyDataSetChanged()
    }
}