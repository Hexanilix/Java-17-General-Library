package org.hetils.jgl17;

import org.jetbrains.annotations.NotNull;

public class FileVersion {
    public static class FileVersionFormatException extends Exception {
        public FileVersionFormatException(String e) {
            super(e);
        }
    }
    /**
     * A function that compares two {@link FileVersion} versions.<br>
     *
     * @return The function returns if the version compared to the base is:<blockquote>
     *     <pre><b>-1</b> <i>older</i></pre>
     *     <pre><b> 0</b> <i>the same</i></pre>
     *     <pre><b> 1</b> <i>newer</i></pre>
     * </blockquote>
     * @param base The FileVersion to compare against
     * @author  Hexanilix
     * @since   1.0
     */
    public int versionDiff(@NotNull FileVersion base) {
        for (int i = 0; i < Math.max(this.ver.length, base.ver.length); i++) {
            int v1 = (i < this.ver.length ? this.ver[i] : 0);
            int v2 = (i < base.ver.length ? base.ver[i] : 0);
            if (v1 > v2) return 1;
            else if (v1 < v2) return -1;
        }
        return 0;
    }
    /**
     * A function that compares two {@link FileVersion} versions.<br>
     *
     * @return The function returns if the comparing version is:<blockquote>
     *     <pre><b>-1</b> - <i>older</i></pre>
     *     <pre><b> 0</b> - <i>the same</i></pre>
     *     <pre><b> 1</b> - <i>newer</i></pre>
     * </blockquote>
     * @param base The FileVersion to compare against
     * @author  Hexanilix
     * @since   1.0
     */
    public static int versionDiff(@NotNull FileVersion comp, @NotNull FileVersion base) {
        for (int i = 0; i < Math.max(comp.ver.length, base.ver.length); i++) {
            int v1 = (i < comp.ver.length ? comp.ver[i] : 0);
            int v2 = (i < base.ver.length ? base.ver[i] : 0);
            if (v1 > v2) return -1;
            else if (v1 < v2) return 1;
        }
        return 0;
    }

    private final int[] ver;

    /**
     *
     * @author  Hexanilix
     * @since   1.0
     */
    public FileVersion(@NotNull String v) {
        String[] s = v.split("\\.");
        ver = new int[s.length];
        try {
            for (int i = 0; i < s.length; i++) {
                try {
                    ver[i] = Integer.parseInt(s[i]);
                } catch (NumberFormatException e) {
                    throw new FileVersionFormatException("Cannot convert String \"" + v + "\" to FileVersion. Supported char combination: x.x.x (n numbers separated by a '.'");
                }
            }
        } catch (FileVersionFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @author  Hexanilix
     * @since   1.0
     */
    public FileVersion(int @NotNull ... versions) {
        ver = new int[versions.length];
        try {
            for (int i = 0; i < versions.length; i++) {
                if (versions[i] < 0) throw new FileVersionFormatException("FileVersion cannot contain negative numbers");
                ver[i] = versions[i];
            }
        } catch (FileVersionFormatException e) {
            e.printStackTrace();
        }
        System.arraycopy(versions, 0, ver, 0, versions.length);
    }
    public FileVersion(boolean safe, @NotNull String v) throws FileVersionFormatException {
        String[] s = v.split("\\.");
        ver = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            try {
                ver[i] = Integer.parseInt(s[i]);
            } catch (NumberFormatException e) {
                throw new FileVersionFormatException("Cannot convert String \"" + v + "\" to FileVersion. Supported char combination: x.x.x (n numbers separated by a '.'");
            }
        }
    }

    /**
     *
     * @author  Hexanilix
     * @since   1.0
     */
    public FileVersion(boolean safe, int @NotNull ... versions) throws FileVersionFormatException {
        ver = new int[versions.length];
        for (int i = 0; i < versions.length; i++) {
            if (versions[i] < 0) throw new FileVersionFormatException("FileVersion cannot contain negative numbers");
            ver[i] = versions[i];
        }
        System.arraycopy(versions, 0, ver, 0, versions.length);
    }

    /**
     * @return The versions in an array
     * @author  Hexanilix
     * @since   1.0
     */
    public int[] getVersions() {
        return ver;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder().append(ver[0]);
        for (int i = 1; i < ver.length; i++) s.append('.').append(ver[i]);
        return s.toString();
    }
}
