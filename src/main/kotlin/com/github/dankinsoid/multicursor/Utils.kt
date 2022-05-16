package com.github.dankinsoid.multicursor

import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.extension.VimExtensionHandler
import com.maddyhome.idea.vim.helper.StringHelper

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
        StringHelper.parseKeys(command(prefix, keys)),
        owner,
        handler,
        false
    )
    VimExtensionFacade.putKeyMapping(
        MappingMode.NVO,
        StringHelper.parseKeys("$prefix$keys"),
        owner,
        StringHelper.parseKeys(command(prefix, keys)),
        true
    )
}

private fun command(prefix: String, keys: String) = "<Plug>(multicursor-$prefix$keys)"
