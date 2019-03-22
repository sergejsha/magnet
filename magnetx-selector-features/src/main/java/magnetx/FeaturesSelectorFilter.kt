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

import android.content.SharedPreferences
import magnet.Classifier
import magnet.Instance
import magnet.SelectorFilter

const val FEATURE_SELECTOR = "features"

/**
 * This class enables selective injection based on boolean values stored in
 * provided shared preferences. Add the library containing this class to your
 * app module to enable feature selector.
 *
 *
 * ### Scope dependencies
 * `@Classifier(FEATURE_SELECTOR) preferences: SharedPreferences`
 *
 * ### Scoping
 * any scoping
 */
@Instance(
    type = SelectorFilter::class,
    classifier = FEATURE_SELECTOR
)
internal class FeaturesSelectorFilter(
    @Classifier(FEATURE_SELECTOR) private val preferences: SharedPreferences
) : SelectorFilter() {

    override fun filter(selector: Array<String>): Boolean {
        check(selector.size == 4) { "Expected selector length 4, actual: ${selector.size}." }

        val operator = when (selector[2]) {
            "==" -> Operator.Equal
            "!=" -> Operator.NotEqual
            else -> error("Supported operators == and !=, actual: ${selector[2]}")
        }

        return operator.apply(
            key = selector[1],
            operand = selector[3].toBoolean(),
            preferences = preferences
        )
    }
  
}

private sealed class Operator {

    abstract fun apply(key: String, operand: Boolean, preferences: SharedPreferences): Boolean

    object Equal : Operator() {
        override fun apply(key: String, operand: Boolean, preferences: SharedPreferences) =
            preferences.getBoolean(key, false) == operand
    }

    object NotEqual : Operator() {
        override fun apply(key: String, operand: Boolean, preferences: SharedPreferences) =
            preferences.getBoolean(key, false) != operand
    }

}
