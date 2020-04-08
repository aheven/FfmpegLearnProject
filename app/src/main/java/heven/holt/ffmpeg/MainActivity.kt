package heven.holt.ffmpeg

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import heven.holt.ffmpeg.adapter.MainAdapter
import heven.holt.ffmpeg.ui.DrawBitmapActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 绘制图片得三种方式
 * 1、ImageView设置
 * 2.SurfaceView绘制
 * 3.自定义view绘制
 */
class MainActivity : AppCompatActivity(), OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val data = mutableListOf("三种方式绘制图片")
        recycler_view.layoutManager = LinearLayoutManager(this)
        val mainAdapter = MainAdapter(data)
        recycler_view.adapter = mainAdapter
        mainAdapter.setOnItemClickListener(this)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        when (position) {
            0 -> DrawBitmapActivity.startAtc()
        }
    }
}
