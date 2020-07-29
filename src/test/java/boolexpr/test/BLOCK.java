package boolexpr.test;

/**
 * Enum representing block numbers.
 *
 * There are only 64 blocks by default because a RegularEnumSet can hold 64
 * values otherwise JumboEnumSet must be used. This number can be expanded with
 * EnumBuster.
 *
 * @author Timothy
 */
public enum BLOCK {
    B0, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, B22, B23, B24, B25, B26, B27, B28, B29, B30, B31, B32, B33, B34, B35, B36, B37, B38, B39, B40, B41, B42, B43, B44, B45, B46, B47, B48, B49, B50, B51, B52, B53, B54, B55, B56, B57, B58, B59, B60, B61, B62, B63;

    @Override
    public String toString() {
        return String.valueOf(ordinal());
    }
}
