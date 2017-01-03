package io.qala.datagen;

import io.qala.datagen.adaptors.CommonsLang3RandomStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.qala.datagen.RandomShortApi.bool;
import static io.qala.datagen.RandomValue.*;

/**
 * <p>Can modify the random string generated by main methods in {@link RandomValue}. Since user specifies the number of
 * characters he expects in the {@code between()} and {@code length()} methods, it's not expected that String Modifier
 * would change that. Therefore the modifiers should replace characters in the random string instead of adding them.</p>
 * <p>Note, that if multiple modifiers are specified, they could overwrite the results of each other - e.g. one could
 * add a special symbol, but then {@link Impls#spaces()} could replace it with the space. So be careful when using
 * multiple modifiers at the same time.</p>
 */
@SuppressWarnings("WeakerAccess")
public interface StringModifier {
    String modify(String original);

    List<String> modify(List<String> original);

    abstract class WithDefaultBatchModify implements StringModifier {
        @Override public List<String> modify(List<String> original) {
            List<String> result = new ArrayList<String>(original.size());
            for (String originalStr : original) {
                result.add(modify(originalStr));
            }
            return result;
        }
    }

    @SuppressWarnings("SameParameterValue") class Impls {
        private Impls() {}
        public static StringModifier spaces() {
            return multipleOf(' ');
        }

        public static StringModifier spaceLeft() {
            return prefix(" ");
        }

        public static StringModifier spacesLeft(int n) {
            char[] prefix = new char[n];
            Arrays.fill(prefix, ' ');
            return prefix(String.valueOf(prefix));
        }

        public static StringModifier spaceRight() {
            return suffix(" ");
        }

        public static StringModifier spacesRight(int n) {
            char[] suffix = new char[n];
            Arrays.fill(suffix, ' ');
            return suffix(String.valueOf(suffix));
        }


        public static StringModifier specialSymbol() {
            /*
             * You can customize what "special" means for your project by globally updating
             * {@link Vocabulary#SPECIAL_SYMBOLS}.
             * @return modifies random string by introducing special characters inside it
             */
            return oneOf(Vocabulary.specialSymbols());
        }

        public static StringModifier oneOf(String chars) {
            return oneOf(chars.toCharArray());
        }

        public static StringModifier oneOf(final char... chars) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    int index = upTo(original.length() - 1).integer();
                    String symbol = CommonsLang3RandomStringUtils.random(1, chars);
                    return new StringBuilder(original).replace(index, index + 1, symbol).toString();
                }
            };
        }

        /**
         * Inserts characters from the specified string to the result.
         *
         * @param chars each of these characters can appear in the resulting string multiple times
         * @return string containing 1 or more of the specified characters, may insert the same symbol multiple times
         * @see #occasional(String)
         */
        public static StringModifier multipleOf(String chars) {
            return multipleOf(chars.toCharArray());
        }

        /**
         * Same as {@link #multipleOf}, but there is a {@code >50%} chance that none of the specified symbols are
         * inserted into resulting string.
         *
         * @param chars each of these characters can appear in the resulting string multiple times
         * @return a modifier that can add multiple characters from the specified string into the resulting random
         * string, but there is a {@code >50%} chance that none of them are going to be inserted
         * @see #multipleOf(String)
         */
        public static StringModifier occasional(final String chars) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    return bool() ? original : multipleOf(chars).modify(original);
                }
            };
        }

        public static StringModifier multipleOf(final char... chars) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    int nOfSymbols = between(1, original.length()).integer();
                    StringBuilder stringBuilder = new StringBuilder(original);
                    for (int i = 0; i < nOfSymbols; i++) {
                        int index = upTo(original.length() - 1).integer();
                        String symbol = length(1).string(chars);
                        stringBuilder.replace(index, index + 1, symbol);
                    }
                    return stringBuilder.toString();
                }
            };
        }

        public static StringModifier prefix(final String prefix) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    if (original.length() < prefix.length())
                        throw new IllegalArgumentException("Prefix cannot be longer than the main string");

                    StringBuilder stringBuilder = new StringBuilder(original);
                    stringBuilder.replace(0, prefix.length(), prefix);
                    return stringBuilder.toString();
                }
            };
        }
        public static StringModifier suffix(final String suffix) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    if(original.length() < suffix.length())
                        throw new IllegalArgumentException("Suffix cannot be longer than the main string");
                    StringBuilder stringBuilder = new StringBuilder(original);
                    stringBuilder.replace(original.length() - suffix.length(), original.length(), suffix);
                    return stringBuilder.toString();
                }
            };
        }
        public static StringModifier whitespaceReplacement(final String replacement) {
            return new WithDefaultBatchModify() {
                @Override public String modify(String original) {
                    StringBuilder stringBuilder = new StringBuilder(original);
                    for(int i = 0; i < original.length(); i++) {
                        if (Character.isWhitespace(original.charAt(i))) {
                            stringBuilder.replace(i, i, replacement);
                        }
                    }
                    return stringBuilder.toString();
                }
            };
        }

    }
}
