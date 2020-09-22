package cpf.sectormenu

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.annotation.Px
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnPreDraw
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Author: cpf
 * Date: 2020/9/18
 * Email: cpf4263@gmail.com
 *
 * 扇形菜单
 */
class SectorMenu : View.OnClickListener {

    /**
     * 扇形中心点
     */
    private lateinit var centerPointF: PointF

    /**
     * 扇形半径
     */
    private var maxRadius = 100.dp

    /**
     * 动画时间，默认300ms
     */
    private var mDuration = 300L

    /**
     * 开始角度
     */
    private var mStartAngle = 0

    /**
     * 结束角度
     */
    private var mEndAngle = 180

    private var mStartInclude = false

    private var mEndInclude = false

    private var mInterpolator: Interpolator = FastOutSlowInInterpolator()

    private lateinit var adapter: SectorMenuAdapter

    private var clickListener: OnSectorMenuItemClickListener? = null

    private var stateListener: OnSectorMenuStateChangedListener? = null

    private var currAnimator: ValueAnimator? = null

    private lateinit var container: ViewGroup

    /**
     * 菜单视图
     */
    private val itemViews = arrayListOf<View>()

    /**
     * 菜单角度
     */
    private val itemAngles = arrayListOf<Int>()

    /**
     * 当前动画半径
     */
    private var mRadius = 0f

    fun isOpen(): Boolean {
        if (mRadius > 0) return true
        return false
    }

    /**
     * 此方法用于获取扇形菜单中心点和添加菜单的容器
     *
     * centerView 的父容器必须为 FrameLayout,RelativeLayout,ConstraintLayout
     */
    fun setCenterView(centerView: View): SectorMenu {
        this.container = centerView.parent as ViewGroup
        centerView.doOnPreDraw {
            this.centerPointF = it.getCenterPointF()
        }
        return this
    }

    fun setRadius(@Px mRadius: Float): SectorMenu {
        this.maxRadius = mRadius
        return this
    }

    fun setDuration(mDuration: Long): SectorMenu {
        this.mDuration = mDuration
        return this
    }

    fun setInterpolator(interpolator: Interpolator): SectorMenu {
        this.mInterpolator = interpolator
        return this
    }

    fun setStartAngle(angle: Int, isIncluded: Boolean): SectorMenu {
        this.mStartAngle = angle
        this.mStartInclude = isIncluded
        return this
    }

    fun setEndAngle(angle: Int, isIncluded: Boolean): SectorMenu {
        this.mEndAngle = angle
        this.mEndInclude = isIncluded
        return this
    }

    fun setAdapter(adapter: SectorMenuAdapter): SectorMenu {
        this.adapter = adapter
        return this
    }

    fun setOnItemClickListener(onSectorMenuItemClickListener: OnSectorMenuItemClickListener): SectorMenu {
        this.clickListener = onSectorMenuItemClickListener
        return this
    }

    fun setStateChangedListener(onSectorMenuStateChangedListener: OnSectorMenuStateChangedListener): SectorMenu {
        this.stateListener = onSectorMenuStateChangedListener
        return this
    }

    fun bindLifecycle(lifeCycleOwner: LifecycleOwner): SectorMenu {
        lifeCycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) currAnimator?.cancel()
            }
        })
        return this
    }

    /**
     * 当数据变更时，需要调用该方法更新
     */
    fun notifyUpdate(): Boolean {
        if (!this::adapter.isInitialized || !this::centerPointF.isInitialized) return false
        itemViews.forEach { container.removeView(it) }
        itemAngles.clear()
        itemViews.clear()
        val offsetCount = when {
            mStartInclude && mEndInclude -> -1
            mStartInclude || mEndInclude -> 0
            else -> 1
        }
        val stepAngle =
            (mEndAngle - mStartAngle) / ((adapter.getCount() + offsetCount).takeIf { it > 0 } ?: 1)
        val startAngle = mStartAngle.takeIf { mStartInclude } ?: (mStartAngle + stepAngle)
        val endAngle = mEndAngle.takeIf { mEndInclude } ?: (mEndAngle - stepAngle)
        for (angle in endAngle downTo startAngle step stepAngle) {
            val itemView = adapter.bindView(itemViews.size)
                .apply {
                    setCenterPointF(centerPointF)
                    alpha = 0f
                    tag = itemViews.size
                    setOnClickListener(this@SectorMenu)
                }
            container.addView(itemView, 0, ViewGroup.LayoutParams(-2, -2))
            itemAngles.add(-angle)
            itemViews.add(itemView)
        }
        return true
    }

    override fun onClick(itemView: View) {
        if (itemView.alpha < 1) return
        clickListener?.onItemClick(itemView.tag as Int, itemView)
    }

    fun open() {
        // 如果已在打开状态则忽略
        if (mRadius > 0) return
        // 如果没有视图则忽略
        if (itemViews.isEmpty() && !notifyUpdate()) return
        ValueAnimator.ofFloat(mRadius, maxRadius)
            .apply {
                currAnimator = this
                duration = mDuration
                interpolator = mInterpolator
                addUpdateListener {
                    mRadius = it.animatedValue as Float
                    itemViews.forEachIndexed { index, view ->
                        view.setCenterPointF(
                            centerPointF.getCirclePointByAngle(mRadius, itemAngles[index])
                        )
                        view.alpha = it.animatedFraction
                    }
                }
                doOnEnd { stateListener?.onOpened() }
            }.start()
    }

    fun close() {
        // 如果已关闭状态则忽略
        if (mRadius < maxRadius) return
        ValueAnimator.ofFloat(mRadius, 0f)
            .apply {
                currAnimator = this
                duration = mDuration
                interpolator = mInterpolator
                addUpdateListener {
                    mRadius = it.animatedValue as Float
                    itemViews.forEachIndexed { index, view ->
                        view.setCenterPointF(
                            centerPointF.getCirclePointByAngle(mRadius, itemAngles[index])
                        )
                        view.alpha = 1 - it.animatedFraction
                    }
                }
                doOnEnd { stateListener?.onClosed() }
            }.start()
    }

    private val Int.dp
        get() = Resources.getSystem().displayMetrics.scaledDensity * this

    private fun View.getCenterPointF(): PointF {
        return PointF(
            x + width / 2,
            y + height / 2
        )
    }

    private fun View.setCenterPointF(pointF: PointF) {
        doOnPreDraw {
            x = pointF.x - width / 2
            y = pointF.y - height / 2
        }
    }

    private fun PointF.getCirclePointByAngle(radius: Float, angle: Int): PointF {
        return PointF(
            ((x + radius * cos(angle * PI / 180)).toFloat()),
            ((y + radius * sin(angle * PI / 180)).toFloat())
        )
    }

}

interface SectorMenuAdapter {
    fun getCount(): Int
    fun bindView(position: Int): View
}

interface OnSectorMenuItemClickListener {
    fun onItemClick(position: Int, view: View)
}

interface OnSectorMenuStateChangedListener {
    fun onOpened()
    fun onClosed()
}