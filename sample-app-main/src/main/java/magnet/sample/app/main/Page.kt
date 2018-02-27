package magnet.sample.app.main

interface Page {

    fun id(): Int
    fun order(): Int
    fun menuTitleId(): Int
    fun menuIconId(): Int
    fun message(): String

}