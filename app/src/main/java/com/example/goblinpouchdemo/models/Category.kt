//Make a data class storing the information you want to pass to the database like this
//It's kind of like a model from asp.net mvc, storing the fields you wanna pass
//Just make sure they have initial values like this one, even empty strings

package com.example.goblinpouchdemo.models

data class Category (
    var name: String = "",
    var description: String = "",
    var totalSpent: Double = 0.0,
)