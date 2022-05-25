package it.cammino.gestionecomunita.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.gestionecomunita.R

fun expandableItem(block: ExpandableItem.() -> Unit): ExpandableItem =
    ExpandableItem().apply(block)

class ExpandableItem : AbstractExpandableItem<ExpandableItem.ViewHolder>(),
    IClickable<ExpandableItem>, ISubItem<ExpandableItem.ViewHolder> {

    var title: StringHolder? = null
        private set
    var setTitle: String? = null
        set(value) {
            title = StringHolder(value)
        }

    var totItems: Int = 0

    var position: Int = 0

    private var mOnClickListener: ((v: View?, adapter: IAdapter<ExpandableItem>, item: ExpandableItem, position: Int) -> Boolean)? =
        null

    override var onItemClickListener: ((v: View?, adapter: IAdapter<ExpandableItem>, item: ExpandableItem, position: Int) -> Boolean)? =
        { v: View?, adapter: IAdapter<ExpandableItem>, item: ExpandableItem, position: Int ->
            v?.let {
                if (!item.isExpanded) {
                    ViewCompat.animate(it.findViewById(R.id.group_indicator)).rotation(180f).start()
                } else {
                    ViewCompat.animate(it.findViewById(R.id.group_indicator)).rotation(0f).start()
                }
            }
            mOnClickListener?.invoke(v, adapter, item, position) ?: true
        }
        set(onClickListener) {
            this.mOnClickListener = onClickListener // on purpose
            field = onClickListener
        }

    override var onPreItemClickListener: ((v: View?, adapter: IAdapter<ExpandableItem>, item: ExpandableItem, position: Int) -> Boolean)? =
        null

    override//this might not be true for your application
    var isSelectable: Boolean
        get() = false
        set(value) {
            super.isSelectable = value
        }

    override val type: Int
        get() = R.id.fastadapter_expandable_item_id

    override val layoutRes: Int
        get() = R.layout.list_group_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(var view: View) : FastAdapter.ViewHolder<ExpandableItem>(view) {

        private var mTitle: TextView? = null
        private var mIndicator: ImageView? = null

        override fun bindView(item: ExpandableItem, payloads: List<Any>) {
            val ctx = itemView.context

            // set the text for the name
            val title = item.title?.getText(ctx)
            val newTitle = "${if (!title.isNullOrEmpty()) title else "N.D."} (${item.totItems})"
            mTitle?.text = newTitle

            if (item.isExpanded)
                mIndicator?.rotation = 0f
            else
                mIndicator?.rotation = 180f
        }

        override fun unbindView(item: ExpandableItem) {
            mTitle?.text = null
            // make sure all animations are stopped
            mIndicator?.clearAnimation()
        }

        init {
            mTitle = view.findViewById(R.id.group_title)
            mIndicator = view.findViewById(R.id.group_indicator)
        }
    }
}
