package net.ultragrav.kcomp.processor

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.ultragrav.kcomp.KComp
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

class KCompTransformer(private val context: IrPluginContext) : IrElementTransformerVoid() {
    private val componentClass = context.referenceClass(ClassId.fromString(Component::class.qualifiedName!!))!!
    private val placeholderClass = context.referenceClass(ClassId.fromString(Placeholder::class.qualifiedName!!))!!
    private val tagResolverClass = context.referenceClass(ClassId.fromString(TagResolver::class.qualifiedName!!))!!
    private val tagResolverType = tagResolverClass.defaultType
    private val tagResolverArrayType = context.irBuiltIns.arrayClass.typeWithArguments(
        listOf(
            makeTypeProjection(tagResolverType, Variance.OUT_VARIANCE)
        )
    )
    private val placeholderComponentFunction = placeholderClass.functionByName("component")

    private val toCompFunctions = context.referenceFunctions(
        CallableId(FqName("net.ultragrav.kcomp"), Name.identifier("toComp"))
    )
    private val toCompStringFunction = toCompFunctions.first {
        it.owner.name == Name.identifier("toComp")
                && it.owner.extensionReceiverParameter?.type == context.irBuiltIns.stringType
    }
    private val toCompListFunction = toCompFunctions.first {
        it.owner.name == Name.identifier("toComp")
                && it.owner.extensionReceiverParameter?.type?.classOrNull == context.irBuiltIns.collectionClass
    }

    override fun visitCall(expression: IrCall): IrExpression {
        super.visitCall(expression)

        val extensionReceiver = expression.extensionReceiver ?: return expression

        val mappedNames = mutableMapOf<String, IrExpression>()

        if (expression.symbol == toCompStringFunction) {
            expression.extensionReceiver = processExpressionString(extensionReceiver, mappedNames)
        } else if (expression.symbol == toCompListFunction) {
            expression.extensionReceiver = processExpressionCollection(expression.extensionReceiver, mappedNames)
        }

        // Nothing to remap
        if (mappedNames.isEmpty()) return expression

        val placeholderVararg = expression.valueArguments[0] as? IrVararg ?: IrVarargImpl(
            0, 0,
            tagResolverArrayType,
            tagResolverType
        ).also { expression.putValueArgument(0, it) }

        mappedNames.mapTo(placeholderVararg.elements) { (name, expr) ->
            val call = IrCallImpl.fromSymbolOwner(0, 0, placeholderComponentFunction)
            call.putValueArgument(0, name.toIrConst(context.irBuiltIns.stringType))
            call.putValueArgument(1, expr)

            call
        }

        return expression
    }

    private fun processExpressionString(
        expression: IrExpression?,
        mappedNames: MutableMap<String, IrExpression>
    ): IrExpression? {
        if (expression is IrStringConcatenation) {
            processStringConcat(expression, mappedNames)
        } else if (expression is IrCall) {
            if (expression.symbol == context.irBuiltIns.memberStringPlus) {
                expression.dispatchReceiver = processExpressionString(expression.dispatchReceiver, mappedNames)
                expression.putValueArgument(0, processExpressionString(expression.getValueArgument(0), mappedNames))
            } else if (!expression.symbol.owner.name.isSpecial
                && (expression.symbol.owner.name.identifier == "trimIndent"
                        || expression.symbol.owner.name.identifier == "replaceIndent")
                && expression.extensionReceiver?.type == context.irBuiltIns.stringType
            ) {
                expression.extensionReceiver = processExpressionString(expression.extensionReceiver, mappedNames)
            }
        } else if (expression is IrGetValueImpl) {
            if (expression.type.isSubtypeOfClass(componentClass)) {
                val name = generateLocalName(mappedNames, expression)
                return "<$name>".toIrConst(context.irBuiltIns.stringType)
            }
        } else if (expression is IrWhen) {
            expression.branches.forEach {
                it.result = processExpressionString(it.result, mappedNames)!!
            }
        }
        return expression
    }

    private fun processExpressionCollection(
        expression: IrExpression?,
        mappedNames: MutableMap<String, IrExpression>
    ): IrExpression? {
        if (expression is IrCall) {
            if (!expression.symbol.owner.name.isSpecial
                && expression.symbol.owner.name.identifier == "listOf"
            ) {
                val listElements = expression.valueArguments[0] as? IrVararg ?: return expression
                listElements.elements.replaceAll {
                    processExpressionString(it as IrExpression, mappedNames)!!
                }
            }
        }
        return expression
    }

    private fun processStringConcat(
        stringConcat: IrStringConcatenation,
        mappedNames: MutableMap<String, IrExpression>
    ) {
        stringConcat.arguments.replaceAll {
            if (it.type.isSubtypeOfClass(componentClass)) {
                val name = generateLocalName(mappedNames, it)
                return@replaceAll "<$name>".toIrConst(context.irBuiltIns.stringType)
            }
            it
        }
    }

    private fun generateLocalName(
        mappedNames: MutableMap<String, IrExpression>,
        expression: IrExpression?
    ): String {
        // Generate new name
        val name = "__local${mappedNames.size}"
        mappedNames[name] = expression!!
        return name
    }

    private fun debug(str: String?) {
        KCompExtension.logger.report(CompilerMessageSeverity.WARNING, str ?: "null")
    }
}