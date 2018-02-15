package com.example.myApplication.constants

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


class SpacesItemDecoration : RecyclerView.ItemDecoration {
    private val space: Int
    private var value = false

    constructor(space: Int) {
        this.space = space
    }

    constructor(space: Int, `val`: Boolean) {
        this.space = space
        this.value = `val`
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = space
        outRect.right = space
        if (value) {
            outRect.bottom = space
            outRect.bottom = space
        }
    }
}
