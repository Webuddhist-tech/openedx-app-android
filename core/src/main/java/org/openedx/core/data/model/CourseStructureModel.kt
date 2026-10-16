package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.data.model.room.BlockDb
import org.openedx.core.data.model.room.CourseStructureEntity
import org.openedx.core.data.model.room.MediaDb
import org.openedx.core.data.model.room.discovery.ProgressDb
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.utils.TimeUtils

data class CourseStructureModel(
    @SerializedName("root")
    val root: String,
    @SerializedName("blocks")
    val blockData: Map<String, Block>,
    @SerializedName("id")
    var id: String?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("number")
    var number: String?,
    @SerializedName("org")
    var org: String?,
    @SerializedName("start")
    var start: String?,
    @SerializedName("start_display")
    var startDisplay: String?,
    @SerializedName("start_type")
    var startType: String?,
    @SerializedName("end")
    var end: String?,
    @SerializedName("courseware_access")
    var coursewareAccess: CoursewareAccess?,
    @SerializedName("media")
    var media: Media?,
    @SerializedName("course_access_details")
    val courseAccessDetails: CourseAccessDetails,
    @SerializedName("certificate")
    val certificate: Certificate?,
    @SerializedName("enrollment_details")
    val enrollmentDetails: EnrollmentDetails,
    @SerializedName("is_self_paced")
    var isSelfPaced: Boolean?,
    @SerializedName("course_progress")
    val progress: Progress?,
) {
    fun mapToDomain(): CourseStructure {
        val normalizedBlocks = blockData.normalizeUnitDescendants()
        return CourseStructure(
            root = root,
            blockData = normalizedBlocks.map {
                it.value.mapToDomain(normalizedBlocks)
            },
            id = id ?: "",
            name = name ?: "",
            number = number ?: "",
            org = org ?: "",
            start = TimeUtils.iso8601ToDate(start ?: ""),
            startDisplay = startDisplay ?: "",
            startType = startType ?: "",
            end = TimeUtils.iso8601ToDate(end ?: ""),
            coursewareAccess = coursewareAccess?.mapToDomain(),
            media = media?.mapToDomain(),
            certificate = certificate?.mapToDomain(),
            isSelfPaced = isSelfPaced ?: false,
            progress = progress?.mapToDomain(),
        )
    }

    fun mapToRoomEntity(): CourseStructureEntity {
        val normalizedBlocks = blockData.normalizeUnitDescendants()
        return CourseStructureEntity(
            root,
            blocks = normalizedBlocks.map { BlockDb.createFrom(it.value) },
            id = id ?: "",
            name = name ?: "",
            number = number ?: "",
            org = org ?: "",
            start = start ?: "",
            startDisplay = startDisplay ?: "",
            startType = startType ?: "",
            end = end ?: "",
            coursewareAccess = coursewareAccess?.mapToRoomEntity(),
            media = MediaDb.createFrom(media),
            certificate = certificate?.mapToRoomEntity(),
            isSelfPaced = isSelfPaced ?: false,
            progress = progress?.mapToRoomEntity() ?: ProgressDb.DEFAULT_PROGRESS,
        )
    }
}

internal fun Map<String, Block>.normalizeUnitDescendants(): Map<String, Block> {
    val translationIds = values
        .filter { it.type == "multilingual_text" }
        .flatMap { it.descendants.orEmpty() }
        .toSet()

    return mapValues { (_, block) ->
        if (block.type != "vertical") {
            block
        } else {
            block.copy(
                descendants = block.descendants.orEmpty()
                    .flatMap { descendantId ->
                        listOf(descendantId) + this[descendantId]?.descendants.orEmpty()
                    }
                    .filterNot(translationIds::contains)
                    .distinct()
            )
        }
    }
}
