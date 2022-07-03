package com.amine.myanimeapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_liste.*


class ListeFragment : Fragment() {

    var animeNameList = ArrayList<String>()
    var animeIdList = ArrayList<Int>()
    private lateinit var ListeAdapter : ListeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ListeAdapter= ListeRecyclerAdapter(animeNameList,animeIdList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ListeAdapter
        sqlDataTaking()
    }

    fun sqlDataTaking(){

        try{

            activity?.let {
                val database = it.openOrCreateDatabase("Animes", Context.MODE_PRIVATE,null)

                val cursor = database.rawQuery("SELECT * FROM animes", null)
                val animenameIndex = cursor.getColumnIndex("animename")
                val animeIdIndex = cursor.getColumnIndex("id")

                animeNameList.clear()
                animeIdList.clear()
                while (cursor.moveToNext()){
                   animeNameList.add(cursor.getString((animeIdIndex)))
                    animeIdList.add(cursor.getInt(animeIdIndex))

                }
                ListeAdapter.notifyDataSetChanged()
                cursor.close()
            }
        }catch(e: Exception){
            e.printStackTrace()
        }
    }


}