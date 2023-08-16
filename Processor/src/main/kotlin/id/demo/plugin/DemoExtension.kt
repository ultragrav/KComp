package id.demo.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

object DemoExtension : IrGenerationExtension {
    lateinit var logger: org.jetbrains.kotlin.cli.common.messages.MessageCollector

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(DemoVisitor(pluginContext), null)
    }
}