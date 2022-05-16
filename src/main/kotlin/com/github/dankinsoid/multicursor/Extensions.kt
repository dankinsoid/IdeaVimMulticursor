package com.github.dankinsoid.multicursor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import kotlin.math.max
import kotlin.math.min


fun IntRange.intersectionWith(other: IntRange): IntRange? {
	return if (this.contains(other.first) || this.contains(other.last) || other.contains(this.last) || other.contains(this.first)) {
		IntRange(max(this.first, other.first), min(this.last, other.last))
	} else {
		null
	}
}

fun Sequence<IntRange>.intersectionsWith(other: IntRange): Sequence<IntRange> {
	return this.mapNotNull { it.intersectionWith(other) }
}

fun Sequence<IntRange>.intersectionsWith(other: Sequence<IntRange>): Sequence<IntRange> {
	return this.flatMap { other.intersectionsWith(it) }
}

fun Editor.selections(): Sequence<IntRange> {
	return this.caretModel.allCarets.mapNotNull {
		if (it.hasSelection()) { IntRange(it.selectionStart, it.selectionEnd) } else { null }
	}.asSequence()
}

fun Editor.addCaret(offset: Int): com.intellij.openapi.editor.Caret? {
	val line = this.document.getLineNumber(offset)
	val lineStart = this.document.getLineStartOffset(line)
	val column = offset - lineStart
	return caretModel.addCaret(VisualPosition(line, column))
}

fun Editor.setCarets(ranges: Sequence<IntRange>, select: Boolean = true, offset: Int = 0) {
	val oldCarets = caretModel.allCarets
	ranges.forEach {
		val caret: com.intellij.openapi.editor.Caret? = if (oldCarets.isNotEmpty()) {
			oldCarets.removeFirst()
		} else {
			addCaret(it.first)
		}
		if (select) {
			caret?.setSelection(it.first + offset, it.last + 1)
		} else {
			caret?.removeSelection()
		}
		caret?.moveToOffset(it.first + offset)
	}
	oldCarets.forEach {
		caretModel.removeCaret(it)
	}
}