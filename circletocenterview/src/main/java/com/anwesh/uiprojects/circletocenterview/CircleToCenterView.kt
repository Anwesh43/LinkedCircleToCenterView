package com.anwesh.uiprojects.circletocenterview

/**
 * Created by anweshmishra on 29/07/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawCTCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / nodes
    val r : Float = gap * 0.4f
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / 60
    paint.color = Color.parseColor("#42A5F5")
    paint.style = Paint.Style.STROKE
    save()
    translate(i * gap + gap/2, h/2)
    for (i in 0..3) {
        save()
        rotate(90f * i)
        drawArc(RectF(-r, -r, r, r), 0f, 90f * (1 - sc1), false, paint)
        drawLine(r - r * sc1, 0f, r - r * sc2, 0f, paint)
        restore()
    }
    restore()
}

class CircleToCenterView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var onAnimationListener : AnimationListener? = null

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> renderer.handleTap()
        }
        return true
    }

    fun addAnimationListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        onAnimationListener = AnimationListener(onComplete, onReset)
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.025f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(30)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }
    }

    data class CTCNode(var i : Int, val state : State = State()) {

        private var next : CTCNode? = null

        private var prev : CTCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CTCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCTCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CTCNode {
            var curr : CTCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedCircleToCenter(var i : Int) {

        private var curr : CTCNode = CTCNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleToCenterView) {

        private val animator : Animator = Animator(view)

        private val lctc : LinkedCircleToCenter = LinkedCircleToCenter(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lctc.draw(canvas, paint)
            animator.animate {
                lctc.update {i, scale ->
                    animator.stop()
                    when (scale) {
                        0f -> view.onAnimationListener?.onReset?.invoke(i)
                        1f -> view.onAnimationListener?.onComplete?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            lctc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : CircleToCenterView {
            val view : CircleToCenterView = CircleToCenterView(activity)
            activity.setContentView(view)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            return view
        }
    }

    data class AnimationListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}
