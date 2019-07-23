package io.rsbox.engine.model.npcdrops

data class NpcDropTableDef(
        val table_rolls: Int,
        val common_table_chance: Double,
        val uncommon_table_chance: Double,
        val rare_table_chance: Double,
        val very_rare_table_chance: Double,
        val always_table: MutableList<NPCDropEntry>,
        val common_table: MutableList<NPCDropEntry>,
        val uncommon_table: MutableList<NPCDropEntry>,
        val rare_table: MutableList<NPCDropEntry>,
        val very_rare_table: MutableList<NPCDropEntry>
        ) {
    companion object {
        private const val DEFAULT_TABLE_ROLLS = 1
        private const val DEFAULT_COMMON_TABLE_CHANCE = 95.8984375
        private const val DEFAULT_UNCOMMON_TABLE_CHANCE = 3.125
        private const val DEFAULT_RARE_TABLE_CHANCE = 0.78125
        private const val DEFAULT_VERY_RARE_TABLE_CHANCE = 0.1953125
        private val DEFAULT_ALWAYS_TABLE = mutableListOf<NPCDropEntry>()
        private val DEFAULT_COMMON_TABLE = mutableListOf<NPCDropEntry>()
        private val DEFAULT_UCOMMON_TABLE = mutableListOf<NPCDropEntry>()
        private val DEFAULT_RARE_TABLE = mutableListOf<NPCDropEntry>()
        private val DEFAULT_VERY_RARE_TABLE = mutableListOf<NPCDropEntry>()

        val DEFAULT = NpcDropTableDef(DEFAULT_TABLE_ROLLS, DEFAULT_COMMON_TABLE_CHANCE, DEFAULT_UNCOMMON_TABLE_CHANCE, DEFAULT_RARE_TABLE_CHANCE, DEFAULT_VERY_RARE_TABLE_CHANCE, DEFAULT_ALWAYS_TABLE, DEFAULT_COMMON_TABLE, DEFAULT_UCOMMON_TABLE, DEFAULT_RARE_TABLE, DEFAULT_VERY_RARE_TABLE)
    }
}