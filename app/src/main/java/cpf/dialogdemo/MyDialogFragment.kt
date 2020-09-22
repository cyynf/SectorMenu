package cpf.dialogdemo

import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import cpf.sectormenu.OnSectorMenuItemClickListener
import cpf.sectormenu.OnSectorMenuStateChangedListener
import cpf.sectormenu.SectorMenu
import cpf.sectormenu.SectorMenuAdapter
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.menu.view.*

/**
 * Author: cpf
 * Date: 2020/9/21
 * Email: cpf4263@gmail.com
 */
class MyDialogFragment : DialogFragment(),
    View.OnClickListener,
    OnSectorMenuStateChangedListener,
    OnSectorMenuItemClickListener,
    DialogInterface.OnShowListener,
    SectorMenuAdapter {

    private lateinit var sectorMenu: SectorMenu

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(0))
                attributes.apply {
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                    windowAnimations = 0
                    horizontalMargin = 0f
                    verticalMargin = 0f
                }
            }
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sectorMenu = SectorMenu()
            .setCenterView(close)
            .bindLifecycle(this)
            .setDuration(300L)
            .setStartAngle(0, false)
            .setEndAngle(180, false)
            .setRadius(resources.displayMetrics.widthPixels * 0.35f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setOnItemClickListener(this)
            .setAdapter(this)
            .setStateChangedListener(this)
        close.setOnClickListener(this)
        dialogContainer.setOnClickListener(this)
        dialog?.setOnShowListener(this)
    }

    override fun onClick(p0: View?) {
        sectorMenu.close()
    }

    override fun onOpened() {

    }

    override fun onClosed() {
        dismiss()
    }

    override fun onItemClick(position: Int, view: View) {
        dismiss()
        Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT)
            .show()
    }

    override fun getCount(): Int = 4

    override fun bindView(position: Int): View {
        return View.inflate(context, R.layout.menu, null).also {
            it.menu_icon.setImageResource(R.mipmap.ic_launcher_round)
            it.menu_text.text = "标题"
        }
    }

    override fun onShow(p0: DialogInterface?) {
        sectorMenu.open()
    }
}