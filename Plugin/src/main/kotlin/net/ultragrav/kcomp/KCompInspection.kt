package net.ultragrav.kcomp

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getAnnotationEntries

class KCompInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KotlinRecursiveElementVisitor() {
            override fun visitCallExpression(expression: KtCallExpression) {
                if (expression.getAnnotationEntries().any {
                        it.calleeExpression?.constructorReferenceExpression?.text ==
                                ComponentPlaceholderInserting::class.qualifiedName
                    }) {
                    holder.registerProblem(expression, "This is a test, let's hope it works :D")
                }
            }
        }
    }
}