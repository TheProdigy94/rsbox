package io.rsbox.api

/**
 * @author Kyle Escobar
 */

enum class Gender(val id: Int) {
    MALE(id = 0),
    FEMALE(id = 1);

    companion object {
        val values = enumValues<Gender>()
    }
}