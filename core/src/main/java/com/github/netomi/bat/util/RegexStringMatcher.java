/*
 *  Copyright (c) 2020 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.github.netomi.bat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Thomas Neidhart
 */
abstract class RegexStringMatcher implements StringMatcher
{
    private final List<Pattern> patterns;

    protected RegexStringMatcher(String separatorCharacters, String regularExpression) {
        patterns = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(regularExpression, ",");
        while (st.hasMoreTokens()) {
            String expression = st.nextToken();
            patterns.add(compilePattern(separatorCharacters, expression));
        }
    }

    private Pattern compilePattern(String separatorCharacters, String expression) {
        // Clean the expression first.
        String cleanedExpression = expression.replaceAll("\\.", "\\\\.");

        // '**' means to match anything till the end.
        // Replace with '@' temporarily to avoid problems with the next rule.
        cleanedExpression = cleanedExpression.replaceAll("\\*\\*", ".@");
        // '*' means to match anything till the next separator character.
        cleanedExpression = cleanedExpression.replaceAll("\\*", "[^" + separatorCharacters + "]*");
        // '?' means to match a single character till the next separator character.
        cleanedExpression = cleanedExpression.replaceAll("\\?", "[^" + separatorCharacters + "]{1}");
        // Replace '@' with '*' at the end.
        cleanedExpression = cleanedExpression.replaceAll("@", "*");

        // '!' is only allowed at the start of the expression and negates it.
        if (cleanedExpression.startsWith("!")) {
            cleanedExpression = "^(?!" + cleanedExpression.substring(1) + "$).*$";
        } else if (cleanedExpression.contains("!")) {
            throw new RuntimeException("'!' only allowed at start of expression.");
        }

        return Pattern.compile(cleanedExpression);
    }

    public boolean matches(String input) {
        for (Pattern p : patterns) {
            if (p.matcher(input).matches()) {
                return true;
            }
        }
        return false;
    }
}
