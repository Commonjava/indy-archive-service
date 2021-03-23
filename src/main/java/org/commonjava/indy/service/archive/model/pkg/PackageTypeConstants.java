package org.commonjava.indy.service.archive.model.pkg;

public class PackageTypeConstants
{
    public static final String PKG_TYPE_MAVEN = "maven";

    public static final String PKG_TYPE_NPM = "npm";

    public static final String PKG_TYPE_GENERIC_HTTP = "generic-http";

    public static boolean isValidPackageType( final String pkgType )
    {
        return PKG_TYPE_MAVEN.equals( pkgType ) || PKG_TYPE_NPM.equals( pkgType ) || PKG_TYPE_GENERIC_HTTP.equals(
                pkgType );
    }
}
