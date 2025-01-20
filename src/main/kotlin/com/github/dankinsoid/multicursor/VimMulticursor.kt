package com.github.dankinsoid.multicursor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.Disposer
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.newapi.vim
import com.maddyhome.idea.vim.state.VimStateMachine
import com.maddyhome.idea.vim.ui.ModalEntry
import java.awt.Font
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import kotlin.math.absoluteValue

class VimMulticursor : VimExtension {
	override fun getName(): String = "multicursor"

	override fun init() {
		val highlightHandler = HighlightHandler()
		mapToFunctionAndProvideKeys("/") { MultiselectSearchHandler(highlightHandler, it) }
		mapToFunctionAndProvideKeys("f") { MultiselectFHandler(0, it) }
		mapToFunctionAndProvideKeys("F") { MultiselectFHandler(0, it) }
		mapToFunctionAndProvideKeys("t") { MultiselectFHandler(-1, it) }
		mapToFunctionAndProvideKeys("T") { MultiselectFHandler(-1, it) }
		mapToFunctionAndProvideKeys("w") { MultiselectHandler("(\\w+)", it) }
		mapToFunctionAndProvideKeys("W") { MultiselectHandler("(?<=\\s|\\A)[^\\s]+", it) }
		mapToFunctionAndProvideKeys("b") { MultiselectHandler("(?<=\\W|\\A)[^\\W]", it) }
		mapToFunctionAndProvideKeys("B") { MultiselectHandler("(?<=\\s|\\A)[^\\s]", it) }
		mapToFunctionAndProvideKeys("e") { MultiselectHandler("[^\\W](?=\\W|\\Z)", it) }
		mapToFunctionAndProvideKeys("E") { MultiselectHandler("[^\\s](?=\\s|\\Z)", it) }
		mapToFunctionAndProvideKeys("ge") { MultiselectHandler("[^\\W](?=\\W|\\Z)", it) }
		mapToFunctionAndProvideKeys("gE") { MultiselectHandler("[^\\s](?=\\s|\\Z)", it) }

		// Text object commands with explicit prefixes
		mapToFunctionAndProvideKeys("ab") { MultiselectTextObjectHandler("(", ")", it) }
		mapToFunctionAndProvideKeys("aB") { MultiselectTextObjectHandler("{", "}", it) }
		mapToFunctionAndProvideKeys("a[") { MultiselectTextObjectHandler("[", "]", it) }
		mapToFunctionAndProvideKeys("a<") { MultiselectTextObjectHandler("<", ">", it) }
		mapToFunctionAndProvideKeys("a\"") { MultiselectTextObjectHandler("\"", "\"", it) }
		mapToFunctionAndProvideKeys("a'") { MultiselectTextObjectHandler("'", "'", it) }
		mapToFunctionAndProvideKeys("a`") { MultiselectTextObjectHandler("`", "`", it) }
		
		// Inside versions
		mapToFunctionAndProvideKeys("ib") { MultiselectTextObjectHandler("(", ")", it) }
		mapToFunctionAndProvideKeys("iB") { MultiselectTextObjectHandler("{", "}", it) }
		mapToFunctionAndProvideKeys("i[") { MultiselectTextObjectHandler("[", "]", it) }
		mapToFunctionAndProvideKeys("i<") { MultiselectTextObjectHandler("<", ">", it) }
		mapToFunctionAndProvideKeys("i\"") { MultiselectTextObjectHandler("\"", "\"", it) }
		mapToFunctionAndProvideKeys("i'") { MultiselectTextObjectHandler("'", "'", it) }
		mapToFunctionAndProvideKeys("i`") { MultiselectTextObjectHandler("`", "`", it) }

		mapToFunctionAndProvideKeys("c", "mc", MulticursorAddHandler(highlightHandler))
		mapToFunctionAndProvideKeys("r", "mc", MulticursorApplyHandler(highlightHandler))
		mapToFunctionAndProvideKeys("d", "mc", MulticursorRemoveHandler(highlightHandler))
	}

	private class MultiselectSearchHandler(
		private val highlightHandler: HighlightHandler,
		private val select: Boolean = false
	) : VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			val panel = injector.commandLine.createWithoutShortcuts(editor.vim, context.vim, " ", "")
			ModalEntry.activate(editor.vim) { key: KeyStroke ->
				return@activate when (key.keyCode) {
					KeyEvent.VK_ESCAPE -> {
						panel.deactivate(refocusOwningEditor = true, resetCaret = true)
						highlightHandler.clearAllMulticursorHighlighters(editor)
						false
					}
					KeyEvent.VK_ENTER -> {
						highlightHandler.clearAllMulticursorHighlighters(editor)
						panel.deactivate(refocusOwningEditor = false, resetCaret = true)
						select(editor, panel.actualText, select)
						false
					}
					else -> {
						panel.handleKey(key)
						highlightHandler.highlightMulticursorRange(editor, ranges(panel.actualText, editor))
						true
					}
				}
			}
			return
		}
	}

	private class MultiselectHandler(private val rexeg: String, private val select: Boolean = false): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			select(editor, rexeg, select)
		}
	}

	private class MultiselectTextObjectHandler(
		private val startDelimiter: String,
		private val endDelimiter: String,
		private val select: Boolean = false
	): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			val offset = editor.caretModel.primaryCaret.offset
			val text = editor.document.charsSequence
			val range = findPairedRange(text, offset, startDelimiter, endDelimiter)
			println(range)
			if (range != null) {
				editor.setCarets(sequenceOf(range), select)
			}
		}

		private fun findPairedRange(text: CharSequence, offset: Int, start: String, end: String): IntRange? {
			// Search both directions from cursor to find closest pair
			var bestStartPos: Int? = null
			var bestEndPos: Int? = null
			var bestDistance = Int.MAX_VALUE

			// Search forward from cursor
			val forwardEnd = findClosingPosition(text, offset, start, end)
			println(forwardEnd)
			if (forwardEnd != null) {
				val forwardStart = findOpeningPosition(text, forwardEnd, start, end)
				println(forwardStart)
				if (forwardStart != null) {
					val distance = (forwardEnd - offset).absoluteValue
					if (distance < bestDistance) {
						bestStartPos = forwardStart
						bestEndPos = forwardEnd
						bestDistance = distance
					}
				}
			}

			// Search backward from cursor
			val backwardStart = findOpeningPosition(text, offset, start, end)
			if (backwardStart != null) {
				val backwardEnd = findClosingPosition(text, backwardStart, start, end)
				if (backwardEnd != null) {
					val distance = (backwardStart - offset).absoluteValue
					if (distance < bestDistance) {
						bestStartPos = backwardStart
						bestEndPos = backwardEnd
						bestDistance = distance
					}
				}
			}

			return if (bestStartPos != null && bestEndPos != null) {
				IntRange(bestStartPos, bestEndPos + end.length - 1)
			} else {
				null
			}
		}

		private fun findClosingPosition(
			text: CharSequence,
			fromOffset: Int,
			start: String,
			end: String,
			maxOffset: Int = text.length
		): Int? {
			var nesting = 0
			var pos = fromOffset
			while (pos < maxOffset) {
				when {
					text.matchesAt(pos, start) -> {
						nesting++
						pos += start.length
					}
					text.matchesAt(pos, end) -> {
						if (nesting == 0) return pos
						nesting--
						pos += end.length
					}
					else -> pos++
				}
			}
			return null
		}

		private fun findOpeningPosition(text: CharSequence, fromOffset: Int, start: String, end: String): Int? {
			var nesting = 0
			var pos = fromOffset
			while (pos >= 0) {
				when {
					text.matchesAt(pos - end.length + 1, end) -> {
						nesting++
						pos -= end.length
					}
					text.matchesAt(pos - start.length + 1, start) -> {
						if (nesting == 0) return pos - start.length + 1
						nesting--
						pos -= start.length
					}
					else -> pos--
				}
			}
			return null
		}

		private fun CharSequence.matchesAt(index: Int, str: String): Boolean {
			if (index + str.length > length) return false
			for (i in str.indices) {
				if (this[index + i] != str[i]) return false
			}
			return true
		}
	}

	private class MultiselectFHandler(private val offset: Int = 0, private val select: Boolean = false): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			val char = getChar(editor) ?: return
			select(editor, "\\$char", select, offset)
		}

		private fun getChar(editor: Editor): Char? {
			val key = VimExtensionFacade.inputKeyStroke(editor)
			return when {
				key.keyChar == KeyEvent.CHAR_UNDEFINED || key.keyCode == KeyEvent.VK_ESCAPE -> null
				else -> key.keyChar
			}
		}
	}

	private class MulticursorAddHandler(private val highlighter: HighlightHandler): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			val offset = editor.caretModel.primaryCaret.offset
			val range = IntRange(offset, offset)
			if (selectedCarets.contains(range)) {
				selectedCarets.remove(range)
				highlighter.clearSingleRange(editor, range)
			} else {
				selectedCarets.add(range)
				highlighter.highlightSingleRange(editor, range)
			}
		}
	}

	private class MulticursorRemoveHandler(private val highlighter: HighlightHandler): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			selectedCarets.clear()
			highlighter.clearAllMulticursorHighlighters(editor)
		}
	}

	private class MulticursorApplyHandler(private val highlighter: HighlightHandler): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			highlighter.clearAllMulticursorHighlighters(editor)
			val offset = editor.caretModel.primaryCaret.offset
			val range = IntRange(offset, offset)
			if (!selectedCarets.contains(range)) {
				selectedCarets.add(0, range)
			}
			editor.setCarets(selectedCarets.asSequence(), false)
			selectedCarets.clear()
		}
	}

	companion object {

		private val selectedCarets: MutableList<IntRange> = mutableListOf()

		private fun select(editor: Editor, regex: String, select: Boolean = true, offset: Int = 0) {
			editor.setCarets(ranges(regex, editor), select, offset)
		}

		private fun ranges(text: String, editor: Editor): Sequence<IntRange> {
			val selections = editor.selections()
			return if (selections.count() > 0) {
				val start = selections.minOf { it.first }
				val chars = editor.document.charsSequence.subSequence(start, selections.maxOf { it.last })
				val ranges = text.toRegex().findAll(chars).map { IntRange(it.range.first + start, it.range.last + start) }
				return ranges.intersectionsWith(selections)
			} else {
				text.toRegex().findAll(editor.document.charsSequence).map { it.range }
			}
		}
	}

	private class HighlightHandler {
		private val sneakHighlighters: MutableSet<RangeHighlighter> = mutableSetOf()

		fun highlightMulticursorRange(editor: Editor, ranges: Sequence<IntRange>) {
			clearAllMulticursorHighlighters(editor)

			val project = editor.project
			if (project != null) {
				Disposer.register(ProjectService.getInstance(project)) {
					sneakHighlighters.clear()
				}
			}

			if (ranges.count() > 0) {
				for (i in 0 until ranges.count()) {
					highlightSingleRange(editor, ranges.elementAt(i))
				}
			}
		}

		fun clearAllMulticursorHighlighters(editor: Editor) {
			sneakHighlighters.forEach { highlighter ->
				editor.markupModel.removeHighlighter(highlighter)
			}
			sneakHighlighters.clear()
		}

		fun highlightSingleRange(editor: Editor, range: IntRange) {
			val highlighter = editor.markupModel.addRangeHighlighter(
				range.first,
				range.last + 1,
				HighlighterLayer.SELECTION,
				getHighlightTextAttributes(editor),
				HighlighterTargetArea.EXACT_RANGE
			)
			sneakHighlighters.add(highlighter)
		}

		fun clearSingleRange(editor: Editor, range: IntRange) {
				sneakHighlighters.first { it.startOffset == range.first }.let {
					editor.markupModel.removeHighlighter(it)
					sneakHighlighters.remove(it)
				}
		}

		private fun getHighlightTextAttributes(editor: Editor) = TextAttributes(
			null,
			EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES.defaultAttributes.backgroundColor,
			editor.colorsScheme.getColor(EditorColors.CARET_COLOR),
			EffectType.SEARCH_MATCH,
			Font.PLAIN
		)
	}
}
