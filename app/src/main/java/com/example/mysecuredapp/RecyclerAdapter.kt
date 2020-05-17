package com.example.mysecuredapp

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mysecuredapp.model.Pet
import com.example.mysecuredapp.util.inflate
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*

class RecyclerAdapter(private val pets: ArrayList<Pet>) :
    RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>() {

    override fun getItemCount() = pets.size

    override fun onBindViewHolder(holder: RecyclerAdapter.PhotoHolder, position: Int) {
        val pet = pets[position]
        holder.bindPet(pet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.PhotoHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return PhotoHolder(inflatedView)
    }

    class PhotoHolder(theView: View) : RecyclerView.ViewHolder(theView), View.OnClickListener {
        private val view = theView
        private var pet: Pet? = null

        init {
            theView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val context = itemView.context
            val showDetailsIntent = Intent(context, PetDetailsActivity::class.java)
            showDetailsIntent.putExtra(PET_KEY, pet)
            context.startActivity(showDetailsIntent)
        }

        fun bindPet(pet: Pet) {
            this.pet = pet
            view.itemName.text = pet.name
            view.itemDate.text = pet.birthday

            val resourceID = itemView.context.resources.getIdentifier(pet.imageResourceName,
                "drawable", itemView.context.packageName)
            view.itemImage.setImageResource(resourceID)
        }

        companion object {
            private const val PET_KEY = "PET"
        }
    }
}