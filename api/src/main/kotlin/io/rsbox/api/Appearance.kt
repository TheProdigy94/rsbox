package io.rsbox.api

/**
 * @author Kyle Escobar
 */

interface Appearance {
    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int


    companion object {

        private val DEFAULT_LOOKS = intArrayOf(9, 14, 109, 26, 33, 36, 42)

        private val DEFAULT_COLORS = intArrayOf(0, 3, 2, 0, 0)

        val DEFAULT = Appearance(
            DEFAULT_LOOKS,
            DEFAULT_COLORS,
            Gender.MALE
        )

        private operator fun invoke(invokedLooks: IntArray, invokedColors: IntArray, invokedGender: Gender): Appearance {
            return Appearance(invokedLooks, invokedColors, invokedGender)
        }
    }
}