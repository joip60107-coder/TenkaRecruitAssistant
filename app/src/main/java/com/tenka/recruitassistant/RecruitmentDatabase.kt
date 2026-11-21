
package com.tenka.recruitassistant

import android.content.Context
import org.json.JSONArray

data class RecruitCharacter(
    val id: String,
    val name: String,
    val rarity: Int,
    val tags: List<String>
)

object RecruitmentDatabase {

    fun load(context: Context): List<RecruitCharacter> {
        val json = context.assets
            .open("characters.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val array = JSONArray(json)
        val characters = mutableListOf<RecruitCharacter>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val tagArray = item.getJSONArray("tags")
            val characterTags = mutableListOf<String>()

            for (j in 0 until tagArray.length()) {
                characterTags.add(tagArray.getString(j))
            }

            characters.add(
                RecruitCharacter(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    rarity = item.getInt("rarity"),
                    tags = characterTags
                )
            )
        }

        return characters
    }

    fun findMatching(
        context: Context,
        selectedTags: List<String>,
        enlistHour: Int
    ): List<RecruitCharacter> {

        if (selectedTags.isEmpty() || selectedTags.size > 3) {
            return emptyList()
        }

        if (enlistHour != 3 && enlistHour != 9) {
            return emptyList()
        }

        val hasLeader = selectedTags.contains("領袖")
        val hasElite = selectedTags.contains("菁英")

        val rarityLimit = when {
            hasLeader -> 4
            enlistHour < 4 && !hasElite -> 2
            else -> 3
        }

        return load(context).filter { character ->
            character.tags.containsAll(selectedTags) &&
            (hasLeader || character.rarity < rarityLimit)
        }
    }
}
