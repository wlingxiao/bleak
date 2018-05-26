package goa.matcher;

import java.util.Comparator;

import static goa.matcher.AntPathMatcher.VARIABLE_PATTERN;

/**
 * <p>In order, the most "generic" pattern is determined by the following:
 * <ul>
 * <li>if it's null or a capture all pattern (i.e. it is equal to "/**")</li>
 * <li>if the other pattern is an actual canMatch</li>
 * <li>if it's a catch-all pattern (i.e. it ends with "**"</li>
 * <li>if it's got more "*" than the other pattern</li>
 * <li>if it's got more "{foo}" than the other pattern</li>
 * <li>if it's shorter than the other pattern</li>
 * </ul>
 */
public class AntPatternComparator implements Comparator<String> {

    private final String path;

    public AntPatternComparator(String path) {
        this.path = path;
    }

    /**
     * Compare two patterns to determine which should canMatch first, i.e. which
     * is the most specific regarding the current path.
     *
     * @return a negative integer, zero, or a positive integer as pattern1 is
     * more specific, equally specific, or less specific than pattern2.
     */
    @Override
    public int compare(String pattern1, String pattern2) {
        PatternInfo info1 = new PatternInfo(pattern1);
        PatternInfo info2 = new PatternInfo(pattern2);

        if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
            return 0;
        } else if (info1.isLeastSpecific()) {
            return 1;
        } else if (info2.isLeastSpecific()) {
            return -1;
        }

        boolean pattern1EqualsPath = pattern1.equals(path);
        boolean pattern2EqualsPath = pattern2.equals(path);
        if (pattern1EqualsPath && pattern2EqualsPath) {
            return 0;
        } else if (pattern1EqualsPath) {
            return -1;
        } else if (pattern2EqualsPath) {
            return 1;
        }

        if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
            return 1;
        } else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
            return -1;
        }

        if (info1.getTotalCount() != info2.getTotalCount()) {
            return info1.getTotalCount() - info2.getTotalCount();
        }

        if (info1.getLength() != info2.getLength()) {
            return info2.getLength() - info1.getLength();
        }

        if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
            return -1;
        } else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
            return 1;
        }

        if (info1.getUriVars() < info2.getUriVars()) {
            return -1;
        } else if (info2.getUriVars() < info1.getUriVars()) {
            return 1;
        }

        return 0;
    }


    /**
     * Value class that holds information about the pattern, e.g. number of
     * occurrences of "*", "**", and "{" pattern elements.
     */
    private static class PatternInfo {

        private final String pattern;

        private int uriVars;

        private int singleWildcards;

        private int doubleWildcards;

        private boolean catchAllPattern;

        private boolean prefixPattern;

        private Integer length;

        public PatternInfo(String pattern) {
            this.pattern = pattern;
            if (this.pattern != null) {
                initCounters();
                this.catchAllPattern = this.pattern.equals("/**");
                this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
            }
            if (this.uriVars == 0) {
                this.length = (this.pattern != null ? this.pattern.length() : 0);
            }
        }

        protected void initCounters() {
            int pos = 0;
            if (this.pattern != null) {
                while (pos < this.pattern.length()) {
                    if (this.pattern.charAt(pos) == '{') {
                        this.uriVars++;
                        pos++;
                    } else if (this.pattern.charAt(pos) == '*') {
                        if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
                            this.doubleWildcards++;
                            pos += 2;
                        } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
                            this.singleWildcards++;
                            pos++;
                        } else {
                            pos++;
                        }
                    } else {
                        pos++;
                    }
                }
            }
        }

        public int getUriVars() {
            return this.uriVars;
        }

        public int getSingleWildcards() {
            return this.singleWildcards;
        }

        public int getDoubleWildcards() {
            return this.doubleWildcards;
        }

        public boolean isLeastSpecific() {
            return (this.pattern == null || this.catchAllPattern);
        }

        public boolean isPrefixPattern() {
            return this.prefixPattern;
        }

        public int getTotalCount() {
            return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
        }

        /**
         * Returns the length of the given pattern, where template variables are considered to be 1 long.
         */
        public int getLength() {
            if (this.length == null) {
                this.length = (this.pattern != null ?
                        VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length() : 0);
            }
            return this.length;
        }
    }
}
