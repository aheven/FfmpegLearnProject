package heven.holt.ffmpeg.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import heven.holt.ffmpeg.R

/**
 *Time:2020/4/7
 *Author:HevenHolt
 *Description:
 */
class MainAdapter(data: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_main, data) {
    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.setText(R.id.content, item)
    }
}