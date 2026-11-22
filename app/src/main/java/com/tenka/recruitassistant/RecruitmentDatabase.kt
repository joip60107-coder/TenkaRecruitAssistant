
package com.tenka.recruitassistant

import android.content.Context
import org.json.JSONArray

data class RecruitCharacter(
    val id: String,
    val name: String,
    val rarity: Int,
    val tags: List<String>
)

data class RecruitRecommendation(
    val tags: List<String>,
    val characters: List<RecruitCharacter>,
    val score: Double,
    val guaranteedSR: Boolean
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

    // 固定使用 9 小時規則，排除領袖組合
    fun findMatching(
        context: Context,
        selectedTags: List<String>,
        enlistHour: Int
    ): List<RecruitCharacter> {

        if (enlistHour != 9 ||
            selectedTags.isEmpty() ||
            selectedTags.size > 3 ||
            selectedTags.contains("領袖")
        ) {
            return emptyList()
        }

        return load(context).filter { character ->
            character.tags.containsAll(selectedTags) &&
            character.rarity < 3
        }
    }

    // 產生 1～3 個標籤的所有組合
    fun findTopRecommendations(
        context: Context,
        detectedTags: List<String>
    ): List<RecruitRecommendation> {

        val tags = detectedTags
            .distinct()
            .filter { it != "領袖" }

        if (tags.isEmpty()) return emptyList()

        val characters = load(context)
        val recommendations = mutableListOf<RecruitRecommendation>()

        fun addCombination(combo: List<String>) {
            val matched = characters.filter { character ->
                character.tags.containsAll(combo) &&
                character.rarity < 3
            }

            if (matched.isEmpty()) return

            val rarityExists = BooleanArray(4)
            matched.forEach { character ->
                if (character.rarity in 0..3) {
                    rarityExists[character.rarity] = true
                }
            }

            var score = 0.0
            rarityExists.forEachIndexed { rarity, exists ->
                if (exists) {
                    score += (rarity - 1.4) * (rarity + 1)
                }
            }

            val guaranteedSR = matched.all {
                it.rarity == 2
            }

            recommendations.add(
                RecruitRecommendation(
                    tags = combo,
                    characters = matched,
                    score = score,
                    guaranteedSR = guaranteedSR
                )
            )
        }

        for (i in tags.indices) {
            addCombination(listOf(tags[i]))

            for (j in i + 1 until tags.size) {
                addCombination(listOf(tags[i], tags[j]))

                for (k in j + 1 until tags.size) {
                    addCombination(
                        listOf(tags[i], tags[j], tags[k])
                    )
                }
            }
        }

        return recommendations
            .sortedWith(
                compareByDescending<RecruitRecommendation> {
                    it.guaranteedSR
                }.thenByDescending {
                    it.score
                }.thenByDescending {
                    it.tags.size
                }
            )
            .take(5)
    }
}
