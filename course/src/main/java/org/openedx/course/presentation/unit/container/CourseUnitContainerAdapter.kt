package org.openedx.course.presentation.unit.container

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.openedx.core.FragmentViewType
import org.openedx.core.domain.model.Block
import org.openedx.core.module.db.DownloadModel
import org.openedx.course.presentation.unit.NotAvailableUnitFragment
import org.openedx.course.presentation.unit.NotAvailableUnitType
import org.openedx.course.presentation.unit.PrerequisiteLockedFragment
import org.openedx.course.presentation.unit.html.HtmlUnitFragment
import org.openedx.course.presentation.unit.video.VideoUnitFragment
import org.openedx.course.presentation.unit.video.YoutubeVideoUnitFragment
import org.openedx.discussion.presentation.threads.DiscussionThreadsFragment
import org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel
import java.io.File

class CourseUnitContainerAdapter(
    fragment: Fragment,
    val blocks: List<Block>,
    private val viewModel: CourseUnitContainerViewModel,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = blocks.size

    override fun createFragment(position: Int): Fragment = unitBlockFragment(blocks[position], position)

    fun getBlock(position: Int): Block = blocks[position]

    // Override getItemId to use block ID as unique identifier
    // This ensures fragments are recreated when block gated status changes
    override fun getItemId(position: Int): Long {
        val block = blocks[position]
        // Create a unique ID based on block ID and its gated status
        // This forces fragment recreation when gated status changes
        val gatedSuffix = if (isBlockGatedWithPrerequisite(block)) "_gated" else "_open"
        return (block.id + gatedSuffix).hashCode().toLong()
    }

    // Override containsItem to check if block still exists
    override fun containsItem(itemId: Long): Boolean {
        return blocks.any { block ->
            val gatedSuffix = if (isBlockGatedWithPrerequisite(block)) "_gated" else "_open"
            (block.id + gatedSuffix).hashCode().toLong() == itemId
        }
    }

    private fun unitBlockFragment(block: Block, position: Int): Fragment {
        val downloadedModel = viewModel.getDownloadModelById(block.id)
        val offlineUrl = downloadedModel?.let { it.path + File.separator + "index.html" } ?: ""
        val noNetwork = !viewModel.hasNetworkConnection

        return when {
            isBlockGatedWithPrerequisite(block) -> {
                createPrerequisiteLockedFragment(block)
            }

            isBlockNotDownloaded(block, noNetwork, offlineUrl) -> {
                createNotAvailableUnitFragment(block, NotAvailableUnitType.NOT_DOWNLOADED, position)
            }

            isBlockOfflineUnsupported(block, noNetwork) -> {
                createNotAvailableUnitFragment(block, NotAvailableUnitType.OFFLINE_UNSUPPORTED, position)
            }

            isVideoBlockAvailable(block) -> {
                createVideoFragment(block, position)
            }

            isDiscussionBlockAvailable(block) -> {
                createDiscussionFragment(block)
            }

            isSupportedHtmlBlock(block) -> {
                createHtmlUnitFragment(block, downloadedModel, noNetwork, offlineUrl, position)
            }

            else -> {
                createNotAvailableUnitFragment(block, NotAvailableUnitType.MOBILE_UNSUPPORTED, position)
            }
        }
    }

    private fun isBlockNotDownloaded(block: Block, noNetwork: Boolean, offlineUrl: String): Boolean {
        return noNetwork && block.isDownloadable && offlineUrl.isEmpty()
    }

    private fun isBlockOfflineUnsupported(block: Block, noNetwork: Boolean): Boolean {
        return noNetwork && !block.isDownloadable
    }

    private fun isVideoBlockAvailable(block: Block): Boolean {
        val encodedVideos = block.studentViewData?.encodedVideos
        val hasVideo = encodedVideos?.hasVideoUrl == true || encodedVideos?.hasYoutubeUrl == true
        return block.isVideoBlock && hasVideo
    }

    private fun isDiscussionBlockAvailable(block: Block): Boolean {
        val topicId = block.studentViewData?.topicId
        return block.isDiscussionBlock && !topicId.isNullOrEmpty()
    }

    private fun isSupportedHtmlBlock(block: Block): Boolean {
        return block.isHTMLBlock ||
                block.isMultilingualTextBlock ||
                block.isProblemBlock ||
                block.isOpenAssessmentBlock ||
                block.isDragAndDropBlock ||
                block.isWordCloudBlock ||
                block.isLTIConsumerBlock ||
                block.isSurveyBlock ||
                block.isLibraryContentBlock
    }

    private fun isBlockGatedWithPrerequisite(block: Block): Boolean {
        val gatedContent = block.gatedContent
        return gatedContent?.gated == true && gatedContent.prereqId.isNotEmpty()
    }

    private fun createHtmlUnitFragment(
        block: Block,
        downloadedModel: DownloadModel?,
        noNetwork: Boolean,
        offlineUrl: String,
        position: Int,
    ): Fragment {
        val lastModified = if (downloadedModel != null && noNetwork) {
            downloadedModel.lastModified ?: ""
        } else {
            ""
        }
        return HtmlUnitFragment.newInstance(
            block.id,
            block.studentViewUrl,
            viewModel.courseId,
            offlineUrl,
            lastModified,
            block.displayName,
            block.type.name,
            position,
            blocks.size,
            !viewModel.isCourseUnitProgressEnabled
        )
    }

    private fun createNotAvailableUnitFragment(
        block: Block,
        type: NotAvailableUnitType,
        position: Int,
    ): Fragment {
        return NotAvailableUnitFragment.newInstance(
            block.id,
            block.lmsWebUrl,
            type,
            block.displayName,
            position,
            blocks.size,
            !viewModel.isCourseUnitProgressEnabled
        )
    }

    private fun createVideoFragment(block: Block, position: Int): Fragment {
        val encodedVideos = block.studentViewData!!.encodedVideos!!
        val transcripts = block.studentViewData!!.transcripts ?: emptyMap()
        val downloadedModel = viewModel.getDownloadModelById(block.id)
        val isDownloaded = downloadedModel != null
        val videoUrl = downloadedModel?.path ?: encodedVideos.videoUrl

        return if (videoUrl.isNotEmpty()) {
            VideoUnitFragment.newInstance(
                block.id,
                viewModel.courseId,
                videoUrl,
                transcripts,
                block.displayName,
                isDownloaded,
                position,
                blocks.size,
                showBlockHeader()
            )
        } else {
            YoutubeVideoUnitFragment.newInstance(
                block.id,
                viewModel.courseId,
                encodedVideos.youtube?.url ?: "",
                transcripts,
                block.displayName,
                position,
                blocks.size,
                showBlockHeader()
            )
        }
    }

    private fun showBlockHeader(): Boolean {
        return !viewModel.isCourseUnitProgressEnabled && viewModel.mode != CourseViewMode.VIDEOS
    }

    private fun createDiscussionFragment(block: Block): Fragment {
        return DiscussionThreadsFragment.newInstance(
            DiscussionTopicsViewModel.TOPIC,
            viewModel.courseId,
            block.studentViewData?.topicId ?: "",
            block.displayName,
            FragmentViewType.MAIN_CONTENT.name,
            block.id
        )
    }

    private fun createPrerequisiteLockedFragment(block: Block): Fragment {
        val gatedContent = block.gatedContent
        return PrerequisiteLockedFragment.newInstance(
            courseId = viewModel.courseId,
            prereqId = gatedContent?.prereqId.orEmpty(),
            prereqSectionName = gatedContent?.prereqSectionName.orEmpty(),
            mode = viewModel.mode
        )
    }
}
