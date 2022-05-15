package com.github.dankinsoid.multicursor

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.helper.StringHelper
import org.jetbrains.plugins.notebooks.editor.hasIntersectionWith
import kotlin.math.max
import kotlin.math.min

/**
 * Map some <Plug>(keys) command to given handler
 *  and create mapping to <Plug>(prefix)[keys]
 */
fun VimExtension.mapToFunctionAndProvideKeys(keys: String, handler: (Boolean) -> VimExtensionHandler) {
    mapToFunctionAndProvideKeys(keys, "Q", handler(true))
    mapToFunctionAndProvideKeys(keys, "q", handler(false))
}

private  fun VimExtension.mapToFunctionAndProvideKeys(keys: String, prefix: String, handler: VimExtensionHandler) {
    val keys = "$prefix$keys"
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NVO,
        StringHelper.parseKeys(command(keys)),
        owner,
        handler,
        false
    )
    VimExtensionFacade.putKeyMapping(
        MappingMode.NVO,
        StringHelper.parseKeys(keys),
        owner,
        StringHelper.parseKeys(command(keys)),
        true
    )
}

private fun command(keys: String) = "<Plug>(multicursor-$keys)"

fun IntRange.intersectionWith(other: IntRange): IntRange? {
    return if (this.hasIntersectionWith(other)) {
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