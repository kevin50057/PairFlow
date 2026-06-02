package com.pairflow.finance;

/** Spec 7.13: 不分帳只記錄 / 平分 / 我請客 / 對方請客 / 自訂比例. */
public enum SplitType {
    NONE,
    EQUAL,
    I_PAID,
    PARTNER_PAID,
    CUSTOM
}
