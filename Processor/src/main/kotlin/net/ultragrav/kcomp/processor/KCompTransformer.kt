package net.ultragrav.kcomp.processor

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.ultragrav.kcomp.ComponentPlaceholderInserting
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
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
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

    private val placeholderInsertingAnnotation =
        context.referenceClass(ClassId.fromString(ComponentPlaceholderInserting::class.qualifiedName!!))!!

    override fun visitCall(expression: IrCall): IrExpression {
        super.visitCall(expression)

        // Expression placeholders
        val mappedNames = mutableMapOf<String, IrExpression>()

        // Only process functions with @ComponentPlaceholderInserting annotation
        if (!expression.symbol.owner.hasAnnotation(placeholderInsertingAnnotation)) return expression

        // Check if last parameter is a vararg of TagResolver
        val lastParam = expression.symbol.owner.valueParameters[expression.symbol.owner.valueParameters.size - 1]
        if (!lastParam.isVararg || lastParam.varargElementType != tagResolverType) {
            throw IllegalStateException("Last parameter of @ComponentPlaceholderInserting function must be a vararg of TagResolver")
        }

        // Update extension receiver (if necessary)
        expression.extensionReceiver = processExpression(expression.extensionReceiver, mappedNames)

        // Update arguments except last one
        for (i in 0 until expression.valueArgumentsCount - 1) {
            expression.putValueArgument(i, processExpression(expression.getValueArgument(i), mappedNames))
        }

        // Nothing to remap
        if (mappedNames.isEmpty()) return expression

        val placeholderVararg =
            expression.valueArguments[expression.valueArgumentsCount - 1] as? IrVararg ?: IrVarargImpl(
                0, 0,
                tagResolverArrayType,
                tagResolverType
            ).also { expression.putValueArgument(expression.valueArgumentsCount - 1, it) }

        mappedNames.mapTo(placeholderVararg.elements) { (name, expr) ->
            val call = IrCallImpl.fromSymbolOwner(0, 0, placeholderComponentFunction)
            call.putValueArgument(0, name.toIrConst(context.irBuiltIns.stringType))
            call.putValueArgument(1, expr)

            call
        }

        return expression
    }

    private fun processExpression(
        expression: IrExpression?,
        mappedNames: MutableMap<String, IrExpression>
    ): IrExpression? {
        if (expression is IrStringConcatenation) {
            expression.arguments.replaceAll {
                if (it.type.isSubtypeOfClass(componentClass)) {
                    val name = generateLocalName(mappedNames, it)
                    return@replaceAll "<$name>".toIrConst(context.irBuiltIns.stringType)
                }
                it
            }
        } else if (expression is IrCall) {
            if (expression.symbol == context.irBuiltIns.memberStringPlus) {
                expression.dispatchReceiver = processExpression(expression.dispatchReceiver, mappedNames)
                expression.putValueArgument(0, processExpression(expression.getValueArgument(0), mappedNames))
            } else if (!expression.symbol.owner.name.isSpecial
                && (expression.symbol.owner.name.identifier == "trimIndent"
                        || expression.symbol.owner.name.identifier == "replaceIndent")
                && expression.extensionReceiver?.type == context.irBuiltIns.stringType
            ) {
                expression.extensionReceiver = processExpression(expression.extensionReceiver, mappedNames)
            } else if (!expression.symbol.owner.name.isSpecial
                && expression.symbol.owner.name.identifier == "listOf"
            ) {
                val listElements = expression.valueArguments[0] as? IrVararg ?: return expression
                listElements.elements.replaceAll {
                    processExpression(it as IrExpression, mappedNames)!!
                }
            }
        } else if (expression is IrGetValueImpl) {
            if (expression.type.isSubtypeOfClass(componentClass)) {
                val name = generateLocalName(mappedNames, expression)
                return "<$name>".toIrConst(context.irBuiltIns.stringType)
            }
        } else if (expression is IrWhen) {
            expression.branches.forEach {
                it.result = processExpression(it.result, mappedNames)!!
            }
        }
        return expression
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