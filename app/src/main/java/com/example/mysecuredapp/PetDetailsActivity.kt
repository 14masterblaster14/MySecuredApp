package com.example.mysecuredapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mysecuredapp.model.Pet
import kotlinx.android.synthetic.main.activity_pet_details.*

class PetDetailsActivity : AppCompatActivity() {

    private var currentPet: Pet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_details)

        currentPet = intent.getSerializableExtra(PET_KEY) as Pet
        nameTextView?.text = currentPet?.name
        birthdayTextView?.text = currentPet?.birthday
        descriptionTextView?.text = currentPet?.medicalNotes

        val resourceID = resources.getIdentifier(
            currentPet?.imageResourceName,
            "drawable", packageName
        )
        photoImageView.setImageResource(resourceID)
    }

    companion object {
        private const val PET_KEY = "PET"
    }
}

