package com.example.mysecuredapp.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable



@Root(name = "pet", strict = false)
data class Pet constructor(@field:Element(name = "name")
                           @param:Element(name = "name")
                           var name: String = "",

                           @field:Element(name = "birthday")
                           @param:Element(name = "birthday")
                           var birthday: String = "",

                           @field:Element(name = "imageResource")
                           @param:Element(name = "imageResource")
                           var imageResourceName: String = "",

                           @field:Element(name = "medicalNotes")
                           @param:Element(name = "medicalNotes")
                           var medicalNotes: String = "") : Serializable