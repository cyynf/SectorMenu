package cpf.dialogdemo

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import cpf.sectormenu.OnSectorMenuItemClickListener
import cpf.sectormenu.SectorMenu
import cpf.sectormenu.SectorMenuAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu.view.*

/**
 * 扇形弹出功能菜单
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 对话框形式
        val dialogFragment = MyDialogFragment()
        open.setOnClickListener {
            dialogFragment.show(supportFragmentManager, null)
        }

        // 悬浮按钮
        val sectorMenu = SectorMenu()
            .bindLifecycle(this@MainActivity)
            .setCenterView(floatBtn)
            .setDuration(300L)
            .setStartAngle(90, true)
            .setEndAngle(180, true)
            .setRadius(100.dp)
            .setInterpolator(FastOutSlowInInterpolator())
            .setOnItemClickListener(object : OnSectorMenuItemClickListener {
                override fun onItemClick(position: Int, view: View) {
                    Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .setAdapter(object : SectorMenuAdapter {
                override fun getCount(): Int = 3

                override fun bindView(): View {
                    return View.inflate(this@MainActivity, R.layout.menu, null).also {
                        it.menu_icon.setImageResource(R.mipmap.ic_launcher_round)
                        it.menu_text.text = "标题"
                    }
                }
            })
        floatBtn.setOnClickListener {
            if (sectorMenu.isOpen()) {
                sectorMenu.close()
            } else {
                sectorMenu.open()
            }
        }
    }

}

private val Int.dp
    get() = Resources.getSystem().displayMetrics.scaledDensity * this
