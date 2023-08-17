package net.ultragrav.kcomp.processor

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

object KCompExtension : IrGenerationExtension {
    lateinit var logger: org.jetbrains.kotlin.cli.common.messages.MessageCollector

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(KCompTransformer(pluginContext), null)
    }
}