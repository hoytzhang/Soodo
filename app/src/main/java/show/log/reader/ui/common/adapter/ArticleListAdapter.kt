package show.log.reader.ui.common.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.databinding.ItemArticleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ArticleListAdapter(
    private val onItemClick: (ArticleWithFeed) -> Unit,
) : ListAdapter<ArticleWithFeed, ArticleListAdapter.ViewHolder>(DiffCallback) {

    var isDarkMode: Boolean = false

    inner class ViewHolder(
        private val binding: ItemArticleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ArticleWithFeed, darkMode: Boolean) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description ?: ""
            binding.tvDate.text = formatRelativeTime(item.published_at)
            binding.tvFeedTitle.text = item.feed_title

            binding.tvTitle.setTypeface(
                null,
                if (item.is_read) Typeface.NORMAL else Typeface.BOLD
            )

            if (!item.image_url.isNullOrBlank()) {
                binding.ivThumbnail.visibility = android.view.View.VISIBLE
                binding.ivThumbnail.load(item.image_url)
            } else {
                binding.ivThumbnail.visibility = android.view.View.GONE
            }

            val titleColor = if (darkMode) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            val metaColor = if (darkMode) 0xFFB0B0B0.toInt() else 0xFF666666.toInt()

            binding.tvTitle.setTextColor(titleColor)
            binding.tvDescription.setTextColor(metaColor)
            binding.tvFeedTitle.setTextColor(metaColor)
            binding.tvDate.setTextColor(metaColor)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isDarkMode)
    }

    private fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes}分钟前"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}小时前"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days}天前"
            }
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ArticleWithFeed>() {
        override fun areItemsTheSame(
            oldItem: ArticleWithFeed,
            newItem: ArticleWithFeed,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ArticleWithFeed,
            newItem: ArticleWithFeed,
        ): Boolean = oldItem == newItem
    }
}
