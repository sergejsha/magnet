/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnetx

import android.os.Build
import magnet.Instance
import magnet.SelectorFilter

@Instance(
    type = SelectorFilter::class,
    classifier = "android"
)
class AndroidSelectorFilter : SelectorFilter() {

    override fun filter(selector: Array<out String>): Boolean {
        check(selector.size >= 4) { "Unexpected selector length: ${selector.size}. $VERSION_ERROR" }
        check(selector[1] == "api") { "Unexpected selector key ${selector[1]}. $VERSION_ERROR" }

        val operator = when (selector[2]) {
            ">=" -> Operator.GreatOrEqual
            "<=" -> Operator.LessOrEqual
            ">" -> Operator.Great
            "<" -> Operator.Less
            "==" -> Operator.Equal
            "!=" -> Operator.NotEqual
            "in" -> Operator.In
            "!in" -> Operator.NotIn
            else -> error("Unsupported operator ${selector[2]}. $VERSION_ERROR")
        }

        return when (operator) {
            is Operator.OneOperand -> operator.apply(operand = selector[3].toInt())
            is Operator.TwoOperands -> {
                check(selector.size > 4) { "Unexpected selector length ${selector.size} for $operator. $VERSION_ERROR" }
                operator.apply(operand1 = selector[3].toInt(), operand2 = selector[4].toInt())
            }
            else -> error("Unsupported operator type: $operator")
        }
    }

    companion object {
        private const val VERSION_ERROR = "Make sure to use same versions of 'magnet' and 'magnetx' packages."
    }

}

private sealed class Operator {

    interface OneOperand {
        fun apply(operand: Int): Boolean
    }

    interface TwoOperands {
        fun apply(operand1: Int, operand2: Int): Boolean
    }

    object NotIn : Operator(), TwoOperands {
        override fun apply(operand1: Int, operand2: Int): Boolean = Build.VERSION.SDK_INT !in operand1..operand2
    }

    object In : Operator(), TwoOperands {
        override fun apply(operand1: Int, operand2: Int): Boolean = Build.VERSION.SDK_INT in operand1..operand2
    }

    object Less : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT < operand
    }

    object Great : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT > operand
    }

    object LessOrEqual : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT <= operand
    }

    object GreatOrEqual : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT >= operand
    }

    object Equal : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT == operand
    }

    object NotEqual : Operator(), OneOperand {
        override fun apply(operand: Int) = Build.VERSION.SDK_INT != operand
    }
}
