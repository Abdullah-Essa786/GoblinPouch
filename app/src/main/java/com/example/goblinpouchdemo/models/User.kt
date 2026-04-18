package com.example.goblinpouchdemo.models

data class User (
    var uid: String = "", //assign the uid from firebase auth to here
    var username: String = "",
    var email: String = "",
    var totalPoints: Int = 0, //For gamification, can leave 0 for now
    var currentAvatar: String = "", //Gamification, can leave blank for now
    var unlockedAvatars: List<String> = emptyList(), //Gamification, can leave empty for now
    var joinDate: String = "" //Can use the SimpleDateFormat to get the date of today
)