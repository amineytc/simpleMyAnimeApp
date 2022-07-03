package com.amine.myanimeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListeRecyclerAdapter(val animeNameList : ArrayList<String>,val idList: ArrayList<Int>) : RecyclerView.Adapter<ListeRecyclerAdapter.AnimeHolder>() {
    class AnimeHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return AnimeHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeHolder, position: Int) {
        holder.itemView.recycler_row_text.text=animeNameList[position]
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToCharacterFragment("camfromerecycler",idList[position])
            Navigation.findNavController(it).navigate(action)

        }
    }

    override fun getItemCount(): Int {
        return animeNameList.size
    }

}