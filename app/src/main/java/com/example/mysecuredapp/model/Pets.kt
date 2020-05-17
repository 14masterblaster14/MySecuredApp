package com.example.mysecuredapp.model

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "pets", strict = false)
data class Pets constructor(@field:ElementList(entry = "pet", inline = true)
                            @param:ElementList(entry = "pet", inline = true)
                            val list: List<Pet>? = null)
