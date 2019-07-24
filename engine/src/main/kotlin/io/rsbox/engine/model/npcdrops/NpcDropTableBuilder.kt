package io.rsbox.engine.model.npcdrops

class NpcDropTableBuilder {
    private var common_table_chance: Double = 0.0
    private var uncommon_table_chance: Double = 0.0
    private var rare_table_chance: Double = 0.0
    private var very_rare_table_chance: Double = 0.0

    private var table_rolls: Int = 1

    private val always_table: MutableList<NPCDropEntry> = mutableListOf()
    private val common_table: MutableList<NPCDropEntry> = mutableListOf()
    private val uncommon_table: MutableList<NPCDropEntry> = mutableListOf()
    private val rare_table: MutableList<NPCDropEntry> = mutableListOf()
    private val very_rare_table: MutableList<NPCDropEntry> = mutableListOf()

    fun getCommonTableChance(): Double { return this.common_table_chance }
    fun getUncommonTableChance(): Double { return this.uncommon_table_chance }
    fun getRareTableChance(): Double { return this.rare_table_chance }
    fun getVeryRareTableChance(): Double { return this.very_rare_table_chance }

    fun getTableRolls(): Int { return this.table_rolls }

    fun getAlwaysTable(): MutableList<NPCDropEntry> { return this.always_table }
    fun getCommonTable(): MutableList<NPCDropEntry> { return this.common_table }
    fun getUncommonTable(): MutableList<NPCDropEntry> { return this.uncommon_table }
    fun getRareTable(): MutableList<NPCDropEntry> { return this.rare_table }
    fun getVeryRareTable(): MutableList<NPCDropEntry> { return this.very_rare_table }

    fun setCommonTableChance(chance: Double) {
        this.common_table_chance = chance
    }

    fun setUncommonTableChance(chance: Double) {
        this.common_table_chance = chance
    }

    fun setRareTableChance(chance: Double) {
        this.rare_table_chance = chance
    }

    fun setVeryRareTableChance(chance: Double) {
        this.very_rare_table_chance = chance
    }

    fun setTableRolls(rolls: Int) {
        this.table_rolls = rolls
    }


    /**
     * Builter of the NpcDropTableDef
     */
    fun build(): NpcDropTableDef {
        return NpcDropTableDef(
                this.table_rolls,
                this.common_table_chance,
                this.uncommon_table_chance,
                this.rare_table_chance,
                this.very_rare_table_chance,
                this.always_table,
                this.common_table,
                this.uncommon_table,
                this.rare_table,
                this.very_rare_table
        )
    }
}