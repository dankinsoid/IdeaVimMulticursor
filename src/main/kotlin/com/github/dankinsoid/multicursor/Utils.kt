package com.github.dankinsoid.multicursor

import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler

/**
 * Map some <Plug>(keys) command to given handler
 *  and create mapping to <Plug>(prefix)[keys]
 */
fun VimExtension.mapToFunctionAndProvideKeys(keys: String, handler: (Boolean) -> VimExtensionHandler) {
    mapToFunctionAndProvideKeys(keys, "mc", handler(false))
    mapToFunctionAndProvideKeys(keys, "ms", handler(true))
}

fun VimExtension.mapToFunctionAndProvideKeys(keys: String, prefix: String, handler: VimExtensionHandler) {
    VimExtensionFacade.putExtensionHandlerMapping(
        MappingMode.NVO,
        injector.parser.parseKeys(command(prefix, keys)),
        owner,
        handler,
        false
    )
    VimExtensionFacade.putKeyMapping(
        MappingMode.NVO,
        injector.parser.parseKeys("$prefix$keys"),
        owner,
        injector.parser.parseKeys(command(prefix, keys)),
        true
    )
}

private fun command(prefix: String, keys: String) = "<Plug>(multicursor-$prefix$keys)"
