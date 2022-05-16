package com.github.dankinsoid.multicursor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.Disposer
import com.maddyhome.idea.vim.VimProjectService
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.option.StrictMode
import com.maddyhome.idea.vim.ui.ModalEntry
import com.maddyhome.idea.vim.ui.ex.ExEntryPanel
import kotlinx.coroutines.selects.whileSelect
import java.awt.Font
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class IdeaVimMulticursor : VimExtension {
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

		mapToFunctionAndProvideKeys("a", "mc", MulticursorAddHandler(highlightHandler))
		mapToFunctionAndProvideKeys("i", "mc", MulticursorApplyHandler(highlightHandler))
		mapToFunctionAndProvideKeys("d", "mc", MulticursorRemoveHandler(highlightHandler))
	}

	private class MultiselectSearchHandler(
		private val highlightHandler: HighlightHandler,
		private val select: Boolean = false
	) : VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			val panel = ExEntryPanel.getInstanceWithoutShortcuts()
			panel.activate(editor, context, " ", "", 1)
			ModalEntry.activate { key: KeyStroke ->
				return@activate when {
					key.keyCode == KeyEvent.VK_ESCAPE -> {
						panel.deactivate(true)
						highlightHandler.clearAllMulticursorHighlighters(editor)
						false
					}
					key.keyCode == KeyEvent.VK_ENTER -> {
						highlightHandler.clearAllMulticursorHighlighters(editor)
						panel.deactivate(false)
						select(editor, panel.text, select)
						false
					} else -> {
						panel.handleKey(key)
						highlightHandler.highlightMulticursorRange(editor, ranges(panel.text, editor))
						true
					}
				}
			}
			return
		}
	}

	/**
	 * This class acts as proxy for normal find commands because we need to update [lastSDirection]
	 */
	private class MultiselectHandler(private val rexeg: String, private val select: Boolean = false): VimExtensionHandler {
		override fun execute(editor: Editor, context: DataContext) {
			select(editor, rexeg, select)
		}
	}

	/**
	 * This class acts as proxy for normal find commands because we need to update [lastSDirection]
	 */
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
				return ranges.intersectionsWith(selections.asSequence())
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
				Disposer.register(VimProjectService.getInstance(project)) {
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
				editor.markupModel.removeHighlighter(highlighter) ?: StrictMode.fail("Highlighters without an editor")
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